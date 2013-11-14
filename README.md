corenlp-json-servlet
====================

A Simple Servlet for Running Stanford's CoreNLP in Memory on a Tomcat Server

Includes [Stanford CoreNLP 2013-06-20](http://nlp.stanford.edu/software/corenlp.shtml) 
``` 
stanford-corenlp-full-2013-06-20/
```

Compile-time requirements:
```
stanford-corenlp-2012-07-09.jar
servlet-api.jar  // from Tomcat 6
```

Runtime requirements:
```
stanford-corenlp-3.2.0.jar
stanford-corenlp-3.2.0-models.jar  // removed for being too large
joda-time.jar
jollyday.jar
servlet-api.jar  // should be in your servlet container
```

Export the servlet as a ```.war``` file and deploy in a Tomcat environment.

The servlet accepts only one parameter, _text_:
```
curl -X POST -d "text=Hello world." http://localhost:8080/CoreNLP/
```

We recommend using [stanford-corenlp-python](https://github.com/dasmith/stanford-corenlp-python) to parse the output string.

