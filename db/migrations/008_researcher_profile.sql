ALTER TABLE users
  ADD COLUMN display_name text CHECK (char_length(display_name) BETWEEN 1 AND 160),
  ADD COLUMN college text CHECK (char_length(college) BETWEEN 1 AND 160);
GRANT UPDATE(display_name, college) ON users TO crm_app;
