#!/usr/bin/env bash

case "$1" in
    "start") docker run -p 49161:1521 -e ORACLE_ALLOW_REMOTE=true -e ORACLE_DISABLE_ASYNCH_IO=true --name oracle-dynattrext -d oracleinanutshell/oracle-xe-11g ;;
     "stop") docker stop oracle-dynattrext && docker rm oracle-dynattrext ;;
          *) echo Unknown command: $1 ;;
esac