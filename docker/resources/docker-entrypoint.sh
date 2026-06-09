#!/bin/sh
exec java -Xms256m -Xmx2048m -Dlog4j.configurationFile=log4j2.xml -Dcjda.localisation.error.name=false -jar ./bot.jar