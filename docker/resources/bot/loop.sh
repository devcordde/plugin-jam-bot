#!/bin/bash


# switch to config directory

while true; do
# Of course you can use any other executable file here. We use java.
  java \
      -Xms256m \
      -Xmx2048m \
      -Dbot.config=config/config.json -Dlog4j.configurationFile=config/log4j2.xml -Dcjda.localisation.error.name=false -Dcjda.interactions.cleanguildcommands=true  \
      -jar ./bot.jar

  code=$?

  case $code in
    0) # proper shutdown
      echo "Performed proper shutdown, exiting restart loop"
      exit 0
    ;;
    10) # restart request
      echo "Requested to restart"
      continue
    ;;
    *) # Recovering
      echo "Unknown exit code, attempting recovery restart in a few seconds"
      sleep 2
      continue
    ;;
  esac
done
