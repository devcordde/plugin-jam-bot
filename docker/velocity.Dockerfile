FROM gradle:jdk25-alpine AS build
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle settings.gradle.kts build.gradle.kts ./
COPY --chown=gradle:gradle backend/build.gradle* ./backend/
COPY --chown=gradle:gradle plugin-api/build.gradle* ./plugin-api/
COPY --chown=gradle:gradle plugin-paper/build.gradle* ./plugin-paper/
COPY --chown=gradle:gradle plugin-velocity/build.gradle* ./plugin-velocity/
RUN mkdir -p plugin-paper/Readme.md && \
    gradle dependencies --no-daemon || true
COPY --chown=gradle:gradle backend ./backend
COPY --chown=gradle:gradle plugin-api ./plugin-api
COPY --chown=gradle:gradle plugin-paper ./plugin-paper
COPY --chown=gradle:gradle plugin-velocity ./plugin-velocity
RUN gradle :plugin-velocity:shadowJar --no-daemon
FROM itzg/mc-proxy:latest
COPY --from=build /home/gradle/project/plugin-velocity/build/libs/plugin-velocity-*-all.jar /plugins/pluginjam.jar
