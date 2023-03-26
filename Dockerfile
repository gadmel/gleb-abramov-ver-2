FROM openjdk:19
ENV ENVIROMENT=prod
MAINTAINER Gleb Abramov <admin@gleb-abramov.com>
EXPOSE 8080
ADD ./backend/target/app.jar app.jar
CMD ["sh", "-c", "java -jar /app.jar", "-Dspring.profiles.active=${MONGODB_URI}"]
