#!/usr/bin/env bash
# Exercises the whole stack through the reverse proxy. Usage: ./docs/smoke-test.sh [base-url]
BASE=${1:-http://localhost}
j() { python3 -c "import sys,json;print(json.load(sys.stdin)['$1'])"; }
set -e
echo "== create user";   U=$(curl -s -X POST $BASE/api/users -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"secret123","name":"Alice","email":"alice@example.com","role":"CLIENT"}' | j id); echo $U
echo "== create device"; D=$(curl -s -X POST $BASE/api/devices -H 'Content-Type: application/json' \
  -d '{"name":"Smart meter 1","description":"Living room","maxConsumption":3.5}' | j id); echo $D
echo "== assign device -> user"; curl -s -X PUT $BASE/api/devices/$D/user -H 'Content-Type: application/json' -d "{\"userId\":\"$U\"}"; echo
echo "== devices of user"; curl -s $BASE/api/devices/user/$U; echo
echo "== delete user (devices get unassigned)"; curl -s -o /dev/null -w "%{http_code}\n" -X DELETE $BASE/api/users/$U
echo "== device after user deletion"; curl -s $BASE/api/devices/$D; echo
echo "== cleanup"; curl -s -o /dev/null -w "%{http_code}\n" -X DELETE $BASE/api/devices/$D
