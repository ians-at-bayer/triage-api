-- ================================================================================
--
-- Schema changes to be applied to dev/test/prod schemas.
--
--
-- Order sometimes matters: place newer changes at the bottom.
-- Delete changes once they are in prod.
-- ================================================================================


-- Migration to Teams
alter table people rename column slack_id to user_id;
alter table settings rename column slack_hook_url to hook_url;
alter table settings rename column slack_message to message;

update settings set hook_url = 'http://teamshookurlgoeshere/';
update settings set message = 'Hi I''m [name] and I''m on support this week. ' ||
                              'If you want to keep up with who is on support, ' ||
                              'try using our [contact card]([cardurl]). ' ||
                              'It always has the latest support info for our team.';