-- ============================================================================
-- users -- User 'postgres' is created by the docker image. The PostgreSQL image
-- sets up trust authentication locally so you may notice a password is not
-- required when connecting from localhost
-- ============================================================================

CREATE USER triagerotationsuser WITH LOGIN PASSWORD 'triagerotationsuser';

-- create this locally so that it exist when restoring from prod into local database
CREATE USER triagerotationsuser_ro WITH LOGIN PASSWORD 'triagerotationsuser_ro';
