FROM ubuntu

RUN apt-get update

# useful things
RUN apt-get install -y net-tools
RUN apt-get install -y iputils-ping
RUN apt-get install -y git

# java
RUN apt-get install -y openjdk-8-jdk
RUN apt-get -f install -y

# sbt
RUN apt-get install -y wget
RUN wget https://dl.bintray.com/sbt/debian/sbt-0.13.7.deb
RUN dpkg -i sbt-0.13.7.deb

WORKDIR ./code

# listen on this port
EXPOSE 8080


