#!/bin/bash
screen -dmS lobby java -Xms1G -Xmx1G -Dcom.mojang.eula.agree=true -jar server.jar --nogui
