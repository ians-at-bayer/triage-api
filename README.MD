# Support Triage Rotations Manager
Assessments Community at Bayer Innovation Day Project by Ian C. Smith

Simple Scala web application for managing support triage rotations between team members. Runs in Tomcat and uses a Postgres database as a backend.

### Run Locally
1. `> cd database; ./rebuild-database.sh; cd ..` Build a database using Docker, ensure Docker is installed first
1. `> sbt ~tomcat:start` Run the application using Tomcat server
1. Browse to localhost:8080

### Deploy to Cloud Foundry
1. Create an encrypted Postgres RDS database accessible from Cloud Foundry
1. Run `06-tables.sql`& `08-reference-data.sql` SQL files in `database/docker-entrypoint-initdb.d` to setup the new db
1. Copy the *.pem file for accessing the encrypted RDS instance and put it in the webapp directory, name it `rds-combined-ca-bundle.pem`    
1. Configure the CF instance to use the new Postgres database by setting properties in `user-deploy/Database.json`, 
   see example file `user-deploy/DatabaseExample.json`
1. Login to CF
1. Go to dir `user-deploy`
1. `> ./services.sh` Setup cloud foundry services
1. Go to project root dir
1. Run `sbt clean package`
1. `> cf-deploy --env=[np/p]` Run the deployment script
