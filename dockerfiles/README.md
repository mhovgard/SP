#Running the application in docker
First you need to install docker on your system https://docs.docker.com/engine/installation/

##Backend development mode
Build you docker image: cd to the /dockerfiles/backend_dev folder and run this command:
```
docker build -t sp_backend_dev .
```
This will take quite a while to do the first time.

When it is done, start the docker container with this line:
```
docker run -i -t -v $PATH_TO_YOUR_REPOSITORY:/code -p 8080:8080 sp_backend_dev
```
this gives us a presistent container, with access to our code, that forwards the 8080 port to our host machine, allowing us to access the webhost. You can add more ports as needed.

We are now looking a bash command line in ubuntu. Use sbt to run the backend code with this line:
```
sbt -ivy /code launch/run
```
this makes sbt save the dependencies locally in the folder lib_managed

##Frontend development mode
Build you docker image: cd to the /dockerfiles/web_dev folder and run this command:
```
docker build -t sp_web_dev .
```

This will take quite a while to do the first time. Start the container with


```
```

Install all dependencies with
```
```

compile the code by tunning  
```
gulp build
```
from the /code folder.

