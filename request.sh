

#!/bin/bash

curl -X 'POST' \
  'http://localhost:8080/api/missions/api/missions' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "scheduledArrival": "2025-12-13T15:20:14.596Z",
  "scheduledDeparture": "2025-12-13T15:20:14.596Z",
  "spacecraftId": 1,
  "commandingOfficerId": 1,
  "priority": "LOW",
  "missionType": "CARGO_TRANSPORT",
  "missionName": "string",
  "missionCode": "Unique"
}'

