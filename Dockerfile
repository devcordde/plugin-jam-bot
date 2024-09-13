# This dockerfile is far from best practice and is a pure "Make it work" approach. Please do not use it as a reference of any kind.
FROM gradle:jdk21-alpine as build

COPY . .
RUN gradle clean :bot:build :plugin-paper:build --no-daemon

# We use a jammy image because we need some more stuff than alpine provides
FROM eclipse-temurin:22-jammy as runtime

WORKDIR /app
RUN apt update
# Make sure screen exists.
RUN apt install -y screen curl

# Setting up the bot
COPY --from=build /home/gradle/bot/build/libs/bot-*-all.jar ./bot.jar
RUN mkdir plugins
RUN mkdir servers
RUN mkdir template
RUN mkdir template/plugins
COPY docker/resources/bot/wait.sh .
# Copy the plugin jam plugin into the template.
COPY --from=build /home/gradle/plugin-paper/build/libs/plugin-paper-*-all.jar ./bot/template/plugins/pluginjam.jar

COPY docker/resources/docker-entrypoint.sh .

EXPOSE 8080

HEALTHCHECK CMD curl --fail http://localhost:8080/swagger-ui || exit 1

ENTRYPOINT ["bash", "docker-entrypoint.sh"]
