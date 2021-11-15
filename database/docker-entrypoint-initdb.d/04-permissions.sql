-- ============================================================================
-- permissions
-- ============================================================================
\connect triage_rotations

GRANT USAGE ON SCHEMA triage_rotations_local TO triagerotationsuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA triage_rotations_local GRANT ALL PRIVILEGES ON TABLES TO triagerotationsuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA triage_rotations_local GRANT USAGE ON SEQUENCES TO triagerotationsuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA triage_rotations_local GRANT SELECT ON TABLES TO triagerotationsuser_ro;
ALTER DEFAULT PRIVILEGES IN SCHEMA triage_rotations_local GRANT USAGE ON SEQUENCES TO triagerotationsuser_ro;