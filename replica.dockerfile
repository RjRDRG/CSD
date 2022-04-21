FROM openjdk:11
COPY config config
COPY keystore keystore
COPY security.conf security.conf
COPY replica/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]