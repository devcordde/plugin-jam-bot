FROM gradle:jdk25-alpine as build
WORKDIR /home/gradle

COPY --chown=gradle:gradle settings.gradle* build.gradle* gradle.properties* ./
COPY --chown=gradle:gradle bot/build.gradle* ./bot/
COPY --chown=gradle:gradle plugin-paper/build.gradle* ./plugin-paper/

RUN gradle dependencies --no-daemon || true

COPY --chown=gradle:gradle . .

RUN gradle :bot:build :plugin-paper:build --no-daemon

# We use a jammy image because we need some more stuff than alpine provides
FROM eclipse-temurin:25-jammy as runtime
WORKDIR /app

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
