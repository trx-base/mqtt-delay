FROM eclipse-temurin:17
RUN mkdir /opt/app
COPY build/libs/mqtt-delay-*-all.jar /opt/app/japp.jar
CMD ["java", "-jar", "/opt/app/japp.jar"]