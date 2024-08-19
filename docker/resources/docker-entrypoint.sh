#!/bin/sh
java -Xms256m -Xmx2048m -Dbot.config=config/config.json -Dlog4j.configurationFile=config/log4j2.xml -Dcjda.localisation.error.name=false -jar ./bot.jar
