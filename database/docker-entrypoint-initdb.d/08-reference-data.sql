-- ============================================================================
-- reference data
-- ============================================================================
\connect triage_rotations
SET SEARCH_PATH TO triage_rotations_local;

insert into settings values
('https://hooks.slack.com/services/<YOUR SLACK WEBHOOK URL>', 0, now(), 7,
 '[name] <@[slackid]> is on support this week. Please reach out to him/her for support issues or see his/her <[card]|contact card> for more information.', 'http://localhost:8080');

insert into people VALUES (default, 'Test User', 'ABC123', 0);
