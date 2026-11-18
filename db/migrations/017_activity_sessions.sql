ALTER TABLE sessions ADD COLUMN family_id uuid NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE sessions ADD COLUMN issued_at timestamptz NOT NULL DEFAULT clock_timestamp();
CREATE INDEX crm_sessions_family ON sessions(family_id);

CREATE FUNCTION crm_renew_session(old_token text,new_token text)
RETURNS boolean LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
DECLARE family uuid; person integer;
BEGIN
  SELECT s.family_id,s.user_id INTO family,person FROM public.sessions s JOIN public.users u ON u.id=s.user_id
    WHERE s.token_hash=old_token AND s.expires_at>clock_timestamp()
      AND u.active AND u.email_verified_at IS NOT NULL AND NOT u.must_change_password;
  IF NOT FOUND THEN RETURN false; END IF;
  PERFORM pg_advisory_xact_lock(hashtextextended(family::text,7722));
  IF NOT EXISTS(SELECT 1 FROM public.sessions WHERE token_hash=old_token AND expires_at>clock_timestamp())
    THEN RETURN false; END IF;
  UPDATE public.sessions SET expires_at=clock_timestamp()+interval '30 minutes' WHERE family_id=family;
  INSERT INTO public.sessions(token_hash,user_id,expires_at,family_id)
    VALUES(new_token,person,clock_timestamp()+interval '30 minutes',family);
  DELETE FROM public.sessions WHERE family_id=family AND issued_at<clock_timestamp()-interval '30 minutes';
  RETURN true;
END $$;
CREATE FUNCTION crm_end_session(token text) RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,public AS $$
DECLARE family uuid;
BEGIN
  SELECT family_id INTO family FROM public.sessions WHERE token_hash=token;
  IF family IS NULL THEN RETURN; END IF;
  PERFORM pg_advisory_xact_lock(hashtextextended(family::text,7722));
  DELETE FROM public.sessions WHERE family_id=family;
END $$;
REVOKE ALL ON FUNCTION crm_renew_session(text,text),crm_end_session(text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION crm_renew_session(text,text),crm_end_session(text) TO crm_app;
