ALTER TABLE users ADD COLUMN email_verified_at timestamptz DEFAULT now();
ALTER TABLE users ADD COLUMN self_registered boolean NOT NULL DEFAULT false;

CREATE TABLE researcher_verification (
  user_id integer PRIMARY KEY REFERENCES users ON DELETE CASCADE,
  code_hash text NOT NULL CHECK (code_hash ~ '^[a-f0-9]{64}$'),
  sent_at timestamptz NOT NULL,
  expires_at timestamptz NOT NULL
);
REVOKE ALL ON researcher_verification FROM PUBLIC, crm_app;

CREATE FUNCTION crm_register_researcher(e text, p text, n text, c text, r text, h text)
RETURNS boolean LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
DECLARE subject integer;
BEGIN
  IF r NOT IN ('student','faculty') THEN RETURN false; END IF;
  INSERT INTO public.users(email,password_hash,display_name,college,role,email_verified_at,self_registered)
    VALUES(e,p,n,c,r,NULL,true) ON CONFLICT (email) DO NOTHING RETURNING id INTO subject;
  IF NOT FOUND THEN RETURN false; END IF;
  INSERT INTO public.researcher_verification VALUES(subject,h,now(),now()+interval '15 minutes');
  RETURN true;
END $$;

CREATE FUNCTION crm_resend_researcher_verification(e text, h text)
RETURNS text LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
DECLARE subject integer;
BEGIN
  SELECT id INTO subject FROM public.users
    WHERE email=e AND active AND self_registered AND email_verified_at IS NULL FOR UPDATE;
  IF NOT FOUND THEN RETURN 'unavailable'; END IF;
  IF EXISTS(SELECT 1 FROM public.researcher_verification WHERE user_id=subject AND code_hash=h) THEN
    RETURN 'collision';
  END IF;
  UPDATE public.researcher_verification SET code_hash=h,sent_at=clock_timestamp(),
    expires_at=clock_timestamp()+interval '15 minutes'
    WHERE user_id=subject AND sent_at <= clock_timestamp()-interval '45 seconds';
  IF NOT FOUND THEN RETURN 'cooldown'; END IF;
  RETURN 'sent';
END $$;

CREATE FUNCTION crm_verify_researcher(e text, h text)
RETURNS boolean LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
DECLARE subject integer;
BEGIN
  SELECT id INTO subject FROM public.users
    WHERE email=e AND active AND self_registered AND email_verified_at IS NULL FOR UPDATE;
  IF NOT FOUND THEN RETURN false; END IF;
  DELETE FROM public.researcher_verification
    WHERE user_id=subject AND code_hash=h AND expires_at>clock_timestamp();
  IF NOT FOUND THEN RETURN false; END IF;
  UPDATE public.users SET email_verified_at=clock_timestamp() WHERE id=subject;
  RETURN true;
END $$;

CREATE FUNCTION crm_login_account(e text)
RETURNS TABLE(id integer,password_hash text,active boolean,email_verified_at timestamptz)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT u.id,u.password_hash,u.active,u.email_verified_at FROM public.users u WHERE u.email=e
$$;

CREATE OR REPLACE FUNCTION crm_session_user(h text) RETURNS TABLE(id integer,role text)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog,public AS $$
  SELECT u.id,u.role FROM public.sessions s JOIN public.users u ON u.id=s.user_id
   WHERE s.token_hash=h AND s.expires_at>now() AND u.active AND u.email_verified_at IS NOT NULL
$$;

REVOKE ALL ON FUNCTION crm_register_researcher(text,text,text,text,text,text),
  crm_resend_researcher_verification(text,text),crm_verify_researcher(text,text),
  crm_login_account(text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION crm_register_researcher(text,text,text,text,text,text),
  crm_resend_researcher_verification(text,text),crm_verify_researcher(text,text),
  crm_login_account(text) TO crm_app;
