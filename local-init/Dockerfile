FROM curlimages/curl

COPY setup.json setup.json
ENTRYPOINT curl -X POST http://api:8080/support-triage-manager-api/v1/setup -H "Content-Type: application/json" -H "user-id: test-user123" -d @setup.json