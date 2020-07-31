#!/usr/bin/env bash
#==============================================================================
# USAGE:
#
# ./database/stash-database.sh      - save the current database container
# ./database/stash-database.sh pop  - restores stashed database container
#
#==============================================================================
# exit script on any error
trap 'exit' ERR

SCRIPT_DIR=$(dirname "$0")
DB_NAME="triage-rotations-db"
DB_STASH="${DB_NAME}-stashed"

#
# UN-STASH: delete current, rename stashed, start stashed.
#
if [[ "$1" = "pop" ]] ; then
    docker rm --force ${DB_NAME} > /dev/null 2>&1 || true
    docker rename ${DB_STASH} ${DB_NAME}
    docker start ${DB_NAME}
    echo "**** DONE: ${DB_STASH} -> ${DB_NAME}"
#
# STASH: rename current, stop current
#
else
    docker rename ${DB_NAME} ${DB_STASH}
    docker stop ${DB_STASH}
    echo "**** DONE: ${DB_NAME} -> ${DB_STASH}"
fi;

