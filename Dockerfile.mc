FROM gradle:jdk25-alpine AS build
WORKDIR /home/gradle
COPY --chown=gradle:gradle settingsEntity.gradle* build.gradle* gradle.properties* ./
COPY --chown=gradle:gradle bot/build.gradle* ./bot/
COPY --chown=gradle:gradle plugin-api/build.gradle* ./plugin-api/
COPY --chown=gradle:gradle plugin-paper/build.gradle* ./plugin-paper/
COPY --chown=gradle:gradle plugin-velocity/build.gradle* ./plugin-velocity/
RUN mkdir -p plugin-paper/Readme.md && \
    gradle dependencies --no-daemon || true
COPY --chown=gradle:gradle plugin-api ./plugin-api
COPY --chown=gradle:gradle plugin-paper ./plugin-paper
RUN gradle :plugin-paper:shadowJar --no-daemon

FROM itzg/minecraft-server:stable-java25
COPY --from=build /home/gradle/plugin-paper/build/libs/plugin-paper-*-all.jar /plugins/pluginjam.jar
