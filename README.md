corenlp-json-servlet
====================

A servlet for running Standford's CoreNLP and Apache's OpenNLP. This should be faster and more stable than [launching a subprocess](https://github.com/dasmith/stanford-corenlp-python) because it keeps the models in memory.

Bundles [Stanford CoreNLP 2013-11-12](http://nlp.stanford.edu/software/corenlp.shtml) 
``` 
lib/stanford-corenlp-full-2013-11-12/
```

Bundles [Apache OpenNLP 1.5.3](http://opennlp.apache.org/cgi-bin/download.cgi)
```
lib/apache-opennlp-1.5.3/
```

Bundles [pre-trained models for the OpenNLP 1.5 series](http://opennlp.sourceforge.net/models-1.5/)
```
bin/WEB-INF/lib/*.bin
bin/WEB-INF/lib/coref/*
```

Bundles [WordNet 3.1 DATABASE FILES](http://www.princeton.edu/wordnet/download/current-version/)
```
bin/WEB-INF/lib/dict/*
```

Note the licenses for bundled libraries.


Compile-time requirements:
```
// CoreNLP
stanford-corenlp-2012-07-09.jar

// OpenNLP
jwnl-1.3.3.jar
opennlp-tools-1.5.3.jar

// JEE
servlet-api.jar
```

Runtime requirements:
```
// CoreNLP
stanford-corenlp-3.2.0.jar
stanford-corenlp-3.2.0-models.jar  // removed for being too large for GitHub
joda-time.jar
jollyday.jar

// OpenNLP
jwnl-1.3.3.jar
opennlp-maxent-3.0.3.jar
opennlp-tools-1.5.3.jar

// JEE
servlet-api.jar  // should already be in your servlet container
```

Export the servlet as a ```.war``` file and deploy in a servlet container.

The servlet accepts only one parameter, _text_:
```
// Defaults to CoreNLP
curl -X POST -d "text=Hello world." http://localhost:8080/nlp/

// CoreNLP
curl -X POST -d "text=Hello world." http://localhost:8080/nlp/CoreNLP

// OpenNLP
curl -X POST -d "text=Hello world." http://localhost:8080/nlp/OpenNLP

```

We have tried to reproduce the command-line output of each tool so the servlet can serve as a drop-in replacement in existing code (e.g. [stanford-corenlp-python](https://github.com/dasmith/stanford-corenlp-python)), but you're welcome to use the included [JSON.simple](https://code.google.com/p/json-simple/) library to output your own JSON.

AWS EC2 Amazon AMI setup:

```
sudo su

# install requirements
yum update -y
yum install tomcat6 java-devel git wget -y

# setup ports
# if you want to change the port (default 8080), edit /etc/tomcat6/server.xml
# update your security group to allow access to 8080 (or whatever port you set)

exit

# download source
mkdir ~/src
cd ~/src
git clone git@github.com:gui-de/corenlp-json-servlet.git

# build war
cd corenlp-json-servlet
rm -rf bin/WEB-INF/classes/
mkdir bin/WEB-INF/classes/
javac -cp /usr/share/java/servlet.jar:lib/stanford-corenlp-full-2013-11-12/stanford-corenlp-3.3.0.jar:lib/apache-opennlp-1.5.3/lib/opennlp-tools-1.5.3.jar -d bin/WEB-INF/classes/ src/*.java
cd bin
jar -cvf ../nlp.war *
cd ..

sudo su

# Copy war to Tomcat
cp nlp.war /var/lib/tomcat6/webapps/

# Copy runtime jars to Tomcat (or you can include them in nlp.war:WEB-INF/lib/)
cp lib/stanford-corenlp-full-2013-11-12/stanford-corenlp-3.3.0.jar /usr/share/tomcat6/lib/
cp lib/stanford-corenlp-full-2013-11-12/joda-time.jar /usr/share/tomcat6/lib/
cp lib/stanford-corenlp-full-2013-11-12/jollyday.jar /usr/share/tomcat6/lib/
cp lib/apache-opennlp-1.5.3/lib/jwnl-1.3.3.jar /usr/share/tomcat6/lib/
cp lib/apache-opennlp-1.5.3/lib/opennlp-maxent-3.0.3.jar /usr/share/tomcat6/lib/
cp lib/apache-opennlp-1.5.3/lib/opennlp-tools-1.5.3.jar /usr/share/tomcat6/lib/

cd ..

# Download and copy models jar to Tomcat (or ship it in the war)
wget "http://nlp.stanford.edu/software/stanford-corenlp-full-2013-11-12.zip"
unzip stanford-corenlp-full-2013-11-12.zip
cp stanford-corenlp-full-2013-11-12/stanford-corenlp-3.3.0-models.jar /usr/share/tomcat6/lib/

# add to /etc/sysconfig/tomcat6
CATALINA_OPTS="-Xms3g -Xms3g -DWNSEARCHDIR=/usr/share/tomcat6/webapps/nlp/WEB-INF/lib/dict/"

service tomcat6 restart
```
