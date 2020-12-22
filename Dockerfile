FROM alpine/git as clone
ARG url
WORKDIR /app
RUN git clone ${url}

FROM maven:3.5-jdk-8-alpine as builder
ARG project
WORKDIR /app
COPY --from=clone /app/${project} /app
RUN mvn package
RUN mvn test -Dtest=AuctionMechanismImplSimulation

FROM openjdk:8-jre-alpine
ARG project
ENV artifact ${project}-1.0-jar-with-dependencies.jar
WORKDIR /app
ENV MASTERIP=127.0.0.1
ENV ID=0
COPY --from=builder /app/target/${artifact} /app

CMD /usr/bin/java -jar ${project} -m $MASTERIP -id $ID
