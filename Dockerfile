FROM openjdk:18
COPY target/zeke-0.0.1-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "zeke-0.0.1-SNAPSHOT.jar"]