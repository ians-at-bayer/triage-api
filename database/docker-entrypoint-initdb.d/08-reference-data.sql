-- ============================================================================
-- reference data
-- ============================================================================
\connect triage_rotations
SET SEARCH_PATH TO triage_rotations_local;

insert into settings values
('https://hooks.slack.com/services/<YOUR SLACK WEBHOOK URL>', -1, now(), 7);
