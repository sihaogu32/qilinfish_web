# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user for safety; /data holds the SQLite survey file and must be
# owned by qilin before the volume is mounted, or the app cannot write to it.
RUN addgroup -S qilin && adduser -S qilin -G qilin \
 && mkdir -p /data && chown qilin:qilin /data

COPY --from=build /app/target/qilinfish-website-*.jar app.jar
RUN chown -R qilin:qilin /app
USER qilin

VOLUME /data
EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"
ENV SURVEY_DB_PATH=/data/survey.db

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
