FROM openjdk:11
COPY config config
COPY keystore keystore
COPY security.conf security.conf
COPY proxy/target/*.jar app.jar