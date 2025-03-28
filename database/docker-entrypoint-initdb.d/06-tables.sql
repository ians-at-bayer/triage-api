-- ============================================================================
-- tables
-- ============================================================================
\connect triage_rotations

SET SEARCH_PATH TO triage_rotations_local;

create table teams (
    id              bigint generated always as identity primary key,
    name            varchar(999)    not null
);

create table people (
	id              serial          not null primary key,
	team_id         int             not null references teams (id) on delete cascade,
	name            varchar(999)    not null,
	user_id         varchar(999)    not null unique,
	order_number    int             not null check (order_number >= 0)
);

create table settings (
    team_id                     int             not null references teams (id) on delete cascade,
	hook_url                    varchar(9999)   not null,
	order_pointer               int             not null,
	next_rotation               timestamp       not null,
	rotation_frequency_days     int             not null,
	message                     varchar(9999)   not null,
    chatbot_id                  varchar(9999)
);



