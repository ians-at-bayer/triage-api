#!/bin/bash
#==============================================================================
# Add the certificates necessary to connect to an encrypted AWS RDS Postgres
# instance to the file where the postgres jdbc driver expect them:
# ~/.postgresql/root.crt
#
# This script is executed by cloud foundry after the application is pushed.
# In this script HOME=/home/vcap/app which is where the war is unzipped and
# where this .profile script will exist. When the application is running
# HOME=HOME=/home/vcap
#==============================================================================

# Make sure the target directory exists.
mkdir --parents "${HOME}/../.postgresql/"

# Move the certs to target location.
mv $HOME/rds-combined-ca-bundle.pem "${HOME}/../.postgresql/root.crt"
