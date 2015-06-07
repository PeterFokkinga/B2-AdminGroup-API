Integration Tests
===

Preparation
---
You need the [Vagrant image]
(http://help.blackboard.com/en-us/Learn/9.1_2014_04/Administrator/080_Developer_Resources/010_Developer_Virtual_Machine)
of Learn 2014.10 to run the integration tests.

Follow the instructions in the link above but replace the included Vagrantfile 
with the Vagrantfile in this directory (it makes the PostgreSQL database 
available on localhost:5432 which is absolutely essential).

After starting the Vagrant image copy the entire `/usr/local/blackboard/config` 
directory into this directory (so you end up with blackboard/config). Run 
`clean-config.sh` to remove the cruft from the config directory and to modify
the external-sql.properties file.

If you are on windows and can't run `clean-config.sh` change external-sql.properties
by hand: remove /usr/local/ (/usr/local/blackboard/config becomes blackboard/config).

Next up: [getting the dependencies](systemlib/readme.md)

Note that there is a file `xythos.properties` in the resources directory for 
tests that is also required. In case you're curious, this file comes from 
`/usr/local/blackboard/apps/tomcat/lib/xythos.properties`

Running tests
---
Simply run `gradlew test` and view the report in `build/reports/tests/index.html`

If you want to run the tests from your IDE be sure to add 
`-Dbbservices_config=blackboard/config/service-config-unittest.properties` to 
the command line / VM options for running tests.
