#!/usr/bin/env bash

PORT="8080"
if [[ -n "$1" ]] ; then
  PORT="$1"
fi


cd $HOME/projects/search

pid=`jps | grep search | awk '{print $1}'`
if [[ -n "$pid" ]] ; then
  echo killing pid $pid
  kill $pid
fi
sleep 2s
echo "Service stopped."

git pull
mvn clean install

# "-d -m" is a special notation to start a new session detached
version=$(grep "<artifactId>search" -A4 pom.xml | grep version | grep -o -E "[0-9\.]+")

screen -d -m -S SearchService bash -c "java -DPORT=${PORT} -Xms64m -Xmx256m -jar target/search-${version}.jar"
echo "Service started..."


