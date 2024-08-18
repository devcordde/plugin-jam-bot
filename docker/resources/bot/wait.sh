#!/usr/bin/env sh
while screen -ls | grep -q "$1"
  do
    sleep 1
    echo "wait"
  done
sleep 1
