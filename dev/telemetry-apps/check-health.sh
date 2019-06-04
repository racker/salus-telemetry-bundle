#!/bin/sh

for p in 8080 8081 8082 8084 8085 8087 8088 8089 8888; do
  echo $p
  curl localhost:$p/actuator/health
  echo
done