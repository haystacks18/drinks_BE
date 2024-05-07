FROM krmp-d2hub-idock.9rum.cc/dev-test/repo_d9d0a3ca0d88
WORKDIR /usr/src/app
COPY package*.json ./
RUN npm ci
COPY . .
RUN apt-get update && \
    apt-get install -y nginx && \
    rm -rf /var/lib/apt/lists/* && \
    rm /etc/nginx/sites-enabled/default
COPY default.conf /etc/nginx/conf.d/
RUN npm install -g serve
CMD npm run build && service nginx start && serve -s build

## Start with a base image containing Java runtime
#FROM openjdk:17-oracle
#
## Add Maintainer Info
#LABEL maintainer="your-email@example.com"
#
## Add a volume pointing to /tmp
#VOLUME /tmp
#
## Make port 8080 available to the world outside this container
#EXPOSE 8080
#
## The application's jar file
#ARG JAR_FILE=build/libs/*.jar
#
## Add the application's jar to the container
#ADD ${JAR_FILE} app.jar
#
## Run the jar file
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
