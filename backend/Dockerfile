FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace
ARG MAVEN_REPO_URL=https://maven.aliyun.com/repository/public
COPY pom.xml .
RUN mkdir -p /root/.m2 && printf '%s\n' \
  '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"' \
  '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' \
  '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">' \
  '  <mirrors>' \
  '    <mirror>' \
  '      <id>custom-repo</id>' \
  '      <mirrorOf>*</mirrorOf>' \
  "      <url>${MAVEN_REPO_URL}</url>" \
  '    </mirror>' \
  '  </mirrors>' \
  '</settings>' > /root/.m2/settings.xml
RUN --mount=type=cache,target=/root/.m2/repository mvn -s /root/.m2/settings.xml -B -q dependency:go-offline -DskipTests -Dmaven.wagon.http.retryHandler.count=5
COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository mvn -s /root/.m2/settings.xml -B clean package -DskipTests -Dmaven.wagon.http.retryHandler.count=5

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/drug-management-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
