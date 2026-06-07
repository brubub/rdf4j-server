FROM eclipse-temurin:21-jdk-ubi10-minimal
WORKDIR /opt/rdf4j-server
ADD https://github.com/brubub/rdf4j-server.git .
RUN ["./gradlew", "clean", "bootJar"]
WORKDIR /home/local/rdf4j-server
ENTRYPOINT ["java", "-jar", "/opt/rdf4j-server/build/libs/rdf4j-server.jar"]
