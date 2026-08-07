FROM maven:3.9.9-eclipse-temurin-21 AS MAVEN_BUILD
COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre
EXPOSE 8080
RUN mkdir -p /app/data
COPY --from=MAVEN_BUILD /target/Portfolio-*.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
