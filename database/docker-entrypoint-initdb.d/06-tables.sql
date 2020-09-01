-- ============================================================================
-- tables
-- ============================================================================
\connect triage_rotations

SET SEARCH_PATH TO triage_rotations_local;

create table people (
	id              serial          not null primary key,
	name            varchar(999)    not null,
	slack_id        varchar(999)    not null,
	order_number    int             not null check (order_number >= 0)
);

create table settings (
	slack_hook_url              varchar(9999)   not null,
	order_pointer               int             not null,
	next_rotation               timestamp       not null,
	rotation_frequency_days     int             not null,
	slack_message               varchar(9999)   not null,
	base_url                    varchar(9999)   not null
);

create table history (
    name        varchar(999)        not null,
    start_time  timestamp           not null,
	end_time    timestamp           not null
);



