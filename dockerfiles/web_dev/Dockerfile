FROM ubuntu

RUN apt-get update

# useful things
RUN apt-get install -y net-tools
RUN apt-get install -y iputils-ping
RUN apt-get install -y git

# node 
RUN apt-get install -y nodejs
RUN apt-get install -y npm
RUN ln -s /usr/bin/nodejs /usr/bin/node #fix naming issue caused by apt
RUN npm install gulp -g
RUN npm install typescript -g
RUN npm install typings -g


