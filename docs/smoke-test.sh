#!/usr/bin/env bash
# Exercises the whole stack through the reverse proxy.
# Usage: ADMIN_PASSWORD=... ./docs/smoke-test.sh [base-url]
BASE=${1:-http://localhost}
ADMIN_USER=${ADMIN_USERNAME:-admin}
ADMIN_PASS=${ADMIN_PASSWORD:?set ADMIN_PASSWORD to the value from .env}
J='Content-Type: application/json'
j() { python3 -c "import sys,json;print(json.load(sys.stdin)['$1'])"; }
code() { curl -s -o /dev/null -w "%{http_code}" "$@"; }
set -e

echo "== no token -> expect 401";           code $BASE/api/devices; echo
echo "== wrong password -> expect 401";     code -X POST $BASE/api/auth/login -H "$J" -d '{"username":"admin","password":"nope-nope"}'; echo
echo "== admin login"
AT=$(curl -s -X POST $BASE/api/auth/login -H "$J" -d "{\"username\":\"$ADMIN_USER\",\"password\":\"$ADMIN_PASS\"}" | j token)
A="Authorization: Bearer $AT"
echo "== admin creates a client";           U=$(curl -s -X POST $BASE/api/users -H "$J" -H "$A" \
  -d '{"username":"alice","password":"secret123","name":"Alice","email":"alice@example.com","role":"CLIENT"}' | j id); echo $U
echo "== admin creates a device";           D=$(curl -s -X POST $BASE/api/devices -H "$J" -H "$A" \
  -d '{"name":"Smart meter 1","description":"Living room","maxConsumption":3.5}' | j id); echo $D
echo "== admin assigns device -> user";     curl -s -X PUT $BASE/api/devices/$D/user -H "$J" -H "$A" -d "{\"userId\":\"$U\"}"; echo
echo "== client login"
CT=$(curl -s -X POST $BASE/api/auth/login -H "$J" -d '{"username":"alice","password":"secret123"}' | j token)
C="Authorization: Bearer $CT"
echo "== client sees own devices -> 200";   code $BASE/api/devices/user/$U -H "$C"; echo
echo "== client lists all devices -> 403";  code $BASE/api/devices -H "$C"; echo
echo "== client lists users -> 403";        code $BASE/api/users -H "$C"; echo
echo "== client deletes device -> 403";     code -X DELETE $BASE/api/devices/$D -H "$C"; echo
echo "== admin deletes user (devices get unassigned)"; code -X DELETE $BASE/api/users/$U -H "$A"; echo
echo "== deleted user's token is rejected -> 401"; code $BASE/api/devices/user/$U -H "$C"; echo
echo "== cleanup";                          code -X DELETE $BASE/api/devices/$D -H "$A"; echo
