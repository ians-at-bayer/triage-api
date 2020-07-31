#!/usr/bin/env bash
#==============================================================================
#
# Builds or rebuilds the database image and container.
#
#==============================================================================
# exit script on any error
trap 'exit' ERR

IMAGE_VERSION="1.0"
IMAGE_NAME="triage-rotations-test-postgres-image"
IMAGE_TAG="${IMAGE_NAME}:${IMAGE_VERSION}"
CONTAINER_NAME="triage-rotations-db"

SCRIPT_DIR=`dirname $0`;


#
# Build image.
#
docker build --no-cache --tag "${IMAGE_TAG}" "${SCRIPT_DIR}/"

#
# Remove the container (even if it is currently running).  Ignore error condition if container does not exist.
#
docker rm --force "${CONTAINER_NAME}"

#
# Create the container.
#
docker create --name "${CONTAINER_NAME}" --restart unless-stopped --publish 15430:5432 "${IMAGE_TAG}"

#
# Start database.
#
docker start "${CONTAINER_NAME}"