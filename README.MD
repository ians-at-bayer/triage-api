# Support Triage Rotations Manager
Assessments Community at Bayer Innovation Day Project by Ian C. Smith

Simple Scala web application for managing support triage rotations between team members. Runs in Tomcat and uses a Postgres database as a backend.

### Run Locally
* In the project root dir run:
   ```shell
   docker compose build
   docker compose up
   ```
* Browse to localhost:8080 
* Note that most endpoints will require the user-id header. Use a browser plugin to add the header with "test-user123" as the value.
