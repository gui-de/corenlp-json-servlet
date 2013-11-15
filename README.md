corenlp-json-servlet
====================

A Simple Servlet for Running Stanford's CoreNLP in Memory on a Tomcat Server

Includes [Stanford CoreNLP 2013-11-12](http://nlp.stanford.edu/software/corenlp.shtml) 
``` 
stanford-corenlp-full-2013-11-12/
```

Compile-time requirements:
```
stanford-corenlp-2012-07-09.jar
servlet-api.jar  // from your servlet container
```

Runtime requirements:
```
stanford-corenlp-3.2.0.jar
stanford-corenlp-3.2.0-models.jar  // removed for being too large
joda-time.jar
jollyday.jar
servlet-api.jar  // should already be in your servlet container
```

Export the servlet as a ```.war``` file and deploy in a Tomcat environment.

The servlet accepts only one parameter, _text_:
```
curl -X POST -d "text=Hello world." http://localhost:8080/CoreNLP/
```

We recommend using [stanford-corenlp-python](https://github.com/dasmith/stanford-corenlp-python) to parse the output string.

AWS EC2 Instructions:

```
sudo su

# install requirements
yum update -y
yum install tomcat6 java-devel git wget -y

# setup ports
# if you want to change the port (default 8080), edit /etc/tomcat6/server.xml
# update your security group to allow access to 8080

exit

# download source
mkdir ~/src
cd ~/src
git clone git@github.com:gui-de/corenlp-json-servlet.git

# build war
cd corenlp-json-servlet
rm -rf bin/WEB-INF/classes/
mkdir bin/WEB-INF/classes/
javac -cp /usr/share/java/servlet.jar:lib/stanford-corenlp-full-2013-11-12/stanford-corenlp-3.3.0.jar -d bin/WEB-INF/classes/ src/CoreNLP.java
cd bin
jar -cvf ../CoreNLP.war *
cd ..

sudo su

# Copy war to Tomcat
cp CoreNLP.war /var/lib/tomcat6/webapps/

# Copy jars to Tomcat (or include them in CoreNLP.war:WEB-INF/lib/)
cp lib/stanford-corenlp-full-2013-11-12/stanford-corenlp-3.3.0.jar /usr/share/tomcat6/lib/
cp lib/stanford-corenlp-full-2013-11-12/joda-time.jar /usr/share/tomcat6/lib/
cp lib/stanford-corenlp-full-2013-11-12/jollyday.jar /usr/share/tomcat6/lib/

cd ..

# Download and copy models jar to Tomcat (or ship them in the war)
wget "http://nlp.stanford.edu/software/stanford-corenlp-full-2013-11-12.zip"
unzip stanford-corenlp-full-2013-11-12.zip
cp stanford-corenlp-full-2013-11-12/stanford-corenlp-3.3.0-models.jar /usr/share/tomcat6/lib/

# add to top of /usr/sbin/tomcat6 (below "#!/bin/bash")
export CATALINA_OPTS="-Xms3g -Xms3g"

service tomcat6 restart
```
