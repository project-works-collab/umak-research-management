ALTER TABLE users ADD COLUMN must_change_password boolean NOT NULL DEFAULT false;
CREATE TABLE crm_password_challenges (
  token_hash text PRIMARY KEY,
  user_id integer NOT NULL REFERENCES users ON DELETE CASCADE,
  password_hash text NOT NULL,
  expires_at timestamptz NOT NULL DEFAULT now()+interval '15 minutes'
);
CREATE TABLE crm_account_history (
  id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  account_id integer NOT NULL REFERENCES users ON DELETE CASCADE,
  actor_id integer REFERENCES users ON DELETE SET NULL,
  action text NOT NULL,
  before_value jsonb NOT NULL,
  after_value jsonb NOT NULL,
  reason text NOT NULL DEFAULT '',
  recorded_at timestamptz NOT NULL DEFAULT clock_timestamp()
);
REVOKE ALL ON crm_password_challenges,crm_account_history FROM PUBLIC,crm_app;

CREATE FUNCTION crm_administers_accounts() RETURNS boolean
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT EXISTS(SELECT 1 FROM public.users u JOIN public.crm_staff_permissions p ON p.user_id=u.id
    WHERE u.id=public.crm_user_id() AND u.role='staff' AND u.active AND p.permission='account_administration')
$$;

CREATE FUNCTION crm_account_directory() RETURNS TABLE(id integer,email text,display_name text,college text,
  role text,active boolean,must_change_password boolean,permissions jsonb)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT u.id,u.email,u.display_name,u.college,u.role,u.active,u.must_change_password,
    coalesce((SELECT jsonb_agg(p.permission ORDER BY p.permission) FROM public.crm_staff_permissions p WHERE p.user_id=u.id),'[]'::jsonb)
  FROM public.users u WHERE public.crm_administers_accounts() ORDER BY u.id
$$;

CREATE FUNCTION crm_account_mutation(target integer,operation text,input jsonb)
RETURNS jsonb LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
DECLARE current public.users; before_state jsonb; after_state jsonb; subject integer; category text;
BEGIN
  IF NOT public.crm_administers_accounts() THEN RETURN jsonb_build_object('error','denied'); END IF;
  PERFORM pg_advisory_xact_lock(7721,1);
  IF operation='create' THEN
    INSERT INTO public.users(email,display_name,college,role,password_hash,must_change_password)
      VALUES(input->>'email',input->>'display_name',input->>'college','staff',input->>'password_hash',true)
      ON CONFLICT(email) DO NOTHING RETURNING id INTO subject;
    IF NOT FOUND THEN RETURN jsonb_build_object('error','duplicate'); END IF;
    INSERT INTO public.crm_staff_permissions(user_id,permission)
      SELECT subject,jsonb_array_elements_text(input->'permissions');
    before_state:='{}'::jsonb;
  ELSE
    SELECT * INTO current FROM public.users WHERE id=target FOR UPDATE;
    IF NOT FOUND THEN RETURN jsonb_build_object('error','missing'); END IF;
    subject:=current.id;
    SELECT to_jsonb(a) INTO before_state FROM public.crm_account_directory() a WHERE a.id=subject;
    IF operation='state' OR operation='permissions' THEN
      IF operation='state' AND NOT (input->>'active')::boolean AND target=public.crm_user_id()
        THEN RETURN jsonb_build_object('error','self'); END IF;
      IF current.active AND current.role='staff'
        AND EXISTS(SELECT 1 FROM public.crm_staff_permissions WHERE user_id=target AND permission='account_administration')
        AND ((operation='state' AND NOT (input->>'active')::boolean)
          OR (operation='permissions' AND NOT (input->'permissions' ? 'account_administration')))
        AND NOT EXISTS(SELECT 1 FROM public.users u JOIN public.crm_staff_permissions p ON p.user_id=u.id
          WHERE u.id<>target AND u.active AND u.role='staff' AND p.permission='account_administration')
        THEN RETURN jsonb_build_object('error','last_admin'); END IF;
      IF operation='permissions' THEN
        IF current.role<>'staff' THEN RETURN jsonb_build_object('error','researcher_permissions'); END IF;
        DELETE FROM public.crm_staff_permissions WHERE user_id=target;
        INSERT INTO public.crm_staff_permissions(user_id,permission)
          SELECT target,jsonb_array_elements_text(input->'permissions');
      ELSE
        UPDATE public.users SET active=(input->>'active')::boolean WHERE id=target;
        IF NOT (input->>'active')::boolean THEN DELETE FROM public.sessions WHERE user_id=target; END IF;
      END IF;
    ELSIF operation='category' THEN
      IF current.role='staff' THEN RETURN jsonb_build_object('error','category'); END IF;
      category:=input->>'category';
      IF category NOT IN ('student','faculty') THEN RETURN jsonb_build_object('error','category'); END IF;
      UPDATE public.users SET role=category WHERE id=target;
    ELSIF operation='recovery' THEN
      IF coalesce(input->>'reason','')='' THEN RETURN jsonb_build_object('error','reason'); END IF;
      UPDATE public.users SET password_hash=input->>'password_hash',must_change_password=true WHERE id=target;
      DELETE FROM public.sessions WHERE user_id=target;
      DELETE FROM public.crm_password_challenges WHERE user_id=target;
    ELSE
      RETURN jsonb_build_object('error','invalid');
    END IF;
  END IF;
  SELECT to_jsonb(a) INTO after_state FROM public.crm_account_directory() a WHERE a.id=subject;
  INSERT INTO public.crm_account_history(account_id,actor_id,action,before_value,after_value,reason)
    VALUES(subject,public.crm_user_id(),operation,before_state,after_state,coalesce(input->>'reason',''));
  RETURN jsonb_build_object('account',after_state);
END $$;

CREATE FUNCTION crm_account_audit(target integer)
RETURNS SETOF public.crm_account_history LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT * FROM public.crm_account_history WHERE account_id=target AND public.crm_administers_accounts() ORDER BY id DESC
$$;

CREATE FUNCTION crm_login_details(address text) RETURNS TABLE(id integer,password_hash text,active boolean,
  email_verified_at timestamptz,must_change_password boolean)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT u.id,u.password_hash,u.active,u.email_verified_at,u.must_change_password FROM public.users u WHERE u.email=address
$$;
CREATE FUNCTION crm_begin_password_change(subject integer,hash text,challenge text)
RETURNS boolean LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
BEGIN
  IF NOT EXISTS(SELECT 1 FROM public.users WHERE id=subject AND password_hash=hash AND active AND must_change_password)
    THEN RETURN false; END IF;
  DELETE FROM public.crm_password_challenges WHERE user_id=subject;
  INSERT INTO public.crm_password_challenges(token_hash,user_id,password_hash) VALUES(challenge,subject,hash);
  RETURN true;
END $$;
CREATE FUNCTION crm_challenge_password(challenge text) RETURNS text
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT c.password_hash FROM public.crm_password_challenges c JOIN public.users u ON u.id=c.user_id
    WHERE c.token_hash=challenge AND c.expires_at>now() AND u.active AND u.must_change_password AND u.password_hash=c.password_hash
$$;
CREATE FUNCTION crm_finish_password_change(challenge text,new_hash text) RETURNS boolean
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
DECLARE subject integer; old_hash text;
BEGIN
  SELECT user_id,password_hash INTO subject,old_hash FROM public.crm_password_challenges
    WHERE token_hash=challenge AND expires_at>now() FOR UPDATE;
  IF NOT FOUND THEN RETURN false; END IF;
  UPDATE public.users SET password_hash=new_hash,must_change_password=false
    WHERE id=subject AND password_hash=old_hash AND active AND must_change_password;
  IF NOT FOUND THEN RETURN false; END IF;
  DELETE FROM public.crm_password_challenges WHERE user_id=subject;
  DELETE FROM public.sessions WHERE user_id=subject;
  INSERT INTO public.crm_account_history(account_id,actor_id,action,before_value,after_value)
    VALUES(subject,subject,'password_replaced','{"must_change_password":true}','{"must_change_password":false}');
  RETURN true;
END $$;
CREATE OR REPLACE FUNCTION crm_session_user(h text) RETURNS TABLE(id integer,role text)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT u.id,u.role FROM public.sessions s JOIN public.users u ON u.id=s.user_id
   WHERE s.token_hash=h AND s.expires_at>now() AND u.active AND u.email_verified_at IS NOT NULL AND NOT u.must_change_password
$$;
REVOKE ALL ON FUNCTION crm_administers_accounts(),crm_account_directory(),crm_account_mutation(integer,text,jsonb),
 crm_account_audit(integer),crm_login_details(text),crm_begin_password_change(integer,text,text),
 crm_challenge_password(text),crm_finish_password_change(text,text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION crm_administers_accounts(),crm_account_directory(),crm_account_mutation(integer,text,jsonb),
 crm_account_audit(integer),crm_login_details(text),crm_begin_password_change(integer,text,text),
 crm_challenge_password(text),crm_finish_password_change(text,text) TO crm_app;
