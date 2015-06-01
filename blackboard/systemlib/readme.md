Integration Test Dependencies
===

The easy way
---
Copy the entire contents of the Blackboard systemlib directory, including its
sub directories, to this directory.

The (slightly) difficult way
---
At the minimum you need to copy these files from the Blackboard systemlib 
directory. These are commercial libraries that are not available from a Maven
repository.
- bb-embedded.jar
- bb-exec.jar
* jdbc/ojdbc6.jar 
* xythos/bb-bbxythos.jar
* xythos/jlansrv.jar
* xythos/xss.jar
* xythos/xsscore.jar

The following libraries are also required, but they are also available from 
Maven Central. Either uncomment the dependencies in the build.gradle or copy
these files from the Blackboard systemlib directory.
* commons-dbcp-1.4.jar
* commons-lang-2.4.jar
* ehcache-core.jar
* kryo-2.24.0.jar
* logj4.jar
* mail.jar
* newrelic-api.jar
* slf4j-api.jar
* slf4j-log4j12.jar
* tomcat-juli.jar
* jdbc/postgresql.jdbc4.jar
* jdbc/tomcat-jdbc.jar
* lucene/lucene-core.jar
* xythos/commons-lang3-3.1.jar 
