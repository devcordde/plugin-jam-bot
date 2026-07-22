# Dev image — source is bind-mounted at /home/gradle/project, the Gradle home
# (~/.gradle) and the project's build/ live in named volumes so incremental
# compilation survives container restarts. Building inside this container is
# as fast as building on the host once the cache is warm.
FROM gradle:jdk25-alpine

WORKDIR /home/gradle/project

# Copy gradlew and gradle directory to ensure they are available if mount fails
# or to have them in the image for better layer caching of dependencies.
COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x gradlew

ENV DOCKER=true
ENV GRADLE_USER_HOME=/home/gradle/.gradle

EXPOSE 8080

# Use the project's wrapper so the Gradle version matches the one pinned in
# gradle/wrapper/gradle-wrapper.properties. The wrapper downloads its
# distribution into GRADLE_USER_HOME on first run; the named volume keeps it
# alive across restarts so subsequent starts skip the download entirely.
# Application plugin's run task is the foreground process so docker compose
# stop / restart cycles it cleanly. Source edits on the host trigger a
# fresh, incremental rebuild on the next restart.
CMD ["./gradlew", "run", "--no-daemon", "-x", "test"]