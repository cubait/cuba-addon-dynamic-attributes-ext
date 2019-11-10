#!/usr/bin/env bash

case "$1" in
    "start") docker run -p 33066:3306 --name mysql-dynattrext -e 'MYSQL_USER=cuba' -e 'MYSQL_PASSWORD=cuba' -e 'MYSQL_DATABASE=dynattrext' -e 'MYSQL_RANDOM_ROOT_PASSWORD=yes' -d mariadb:10.0 ;;
     "stop") docker stop mysql-dynattrext && docker rm mysql-dynattrext ;;
          *) echo Unknown command: $1 ;;
esac