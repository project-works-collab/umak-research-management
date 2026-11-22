DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='crm_tfidf') THEN
    CREATE ROLE crm_tfidf NOLOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION;
  END IF;
END $$;
DO $$ BEGIN
  EXECUTE format('GRANT CONNECT ON DATABASE %I TO crm_tfidf', current_database());
END $$;
GRANT USAGE ON SCHEMA public TO crm_tfidf;
GRANT SELECT(id,title,abstract) ON public.research_records TO crm_tfidf;
CREATE POLICY tfidf_archive_read ON public.research_records FOR SELECT TO crm_tfidf USING (true);
