-- Registry managers already read these registered records through crm_registry_record.
-- Permit the same registered catalogue rows in trigram/TF-IDF candidate retrieval;
-- unregistered private drafts retain their owner/member/assigned-reviewer scope.
CREATE POLICY registered_registry_read ON public.research_records
  FOR SELECT TO crm_app USING (is_registered AND public.crm_manages_registry());
