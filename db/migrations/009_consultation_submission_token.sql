ALTER TABLE consultation_tickets
  ADD COLUMN submission_token uuid,
  ADD COLUMN submission_fingerprint text,
  ADD CHECK ((submission_token IS NULL) = (submission_fingerprint IS NULL));

CREATE UNIQUE INDEX consultation_submission_token
  ON consultation_tickets (requester_id, submission_token);
