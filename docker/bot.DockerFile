FROM openjdk:18

WORKDIR /app

ADD bot/build/libs/bot-1.0-all.jar bot.jar
ADD conf/dev/config.json config.json

ENTRYPOINT ["java", "-Dbot.config=./config.json","-jar", "bot.jar"]
