-- ================================================================================
--
-- Schema changes to be applied to dev/test/prod schemas.
--
--
-- Order sometimes matters: place newer changes at the bottom.
-- Delete changes once they are in prod.
-- ================================================================================

--Chatbot support
alter table settings add column chatbot_id varchar(9999);