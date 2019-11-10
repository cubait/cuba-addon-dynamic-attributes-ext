#!/usr/bin/env bash

case "$1" in
    "start") docker run -p 54322:5432 --name postgres-dynattrext -e 'POSTGRES_USER=cuba' -e 'POSTGRES_PASSWORD=cuba' -e 'POSTGRES_DB=dynattrext' -d postgres:9-alpine ;;
     "stop") docker stop postgres-dynattrext && docker rm postgres-dynattrext ;;
          *) echo Unknown command: $1 ;;
esac