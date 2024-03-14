#!/bin/sh
set -e
exec java -jar /tmp/codecrafters-redis-target/java_redis.jar "$@"