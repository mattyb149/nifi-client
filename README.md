nifi-client
------------------------------------
nifi-client is a JVM client library for interacting with an Apache NiFi instance. It includes a Gradle task that 
provides an interactive Groovy Shell with the nifi-client library already loaded. See the Usage section for more details.


Project Structure
------------------------------------

nifi-client is a basic Groovy application (originally created from a lazybones groovy-app template. There is a 
standard Maven/Gradle project structure for source code and tests.


Usage
------------------------------------

Start the shell (use 'gradlew' rather than 'gradle' if you do not have Gradle installed):

gradle —no-daemon shell


Then to connect to your instance:

nifi = NiFi.bind('http://127.0.0.1:8080')


To get a map of templates (the map is name -> properties):

nifi.templates


To upload a template from a file:

nifi.templates << '/Users/mburgess/datasets/GetUserData.xml’


To instantiate a template, fetch by name and call instantiate:

nifi.templates.'GetUserData'.instantiate()


To export a template:

nifi.templates.’GetUserData’ >> ‘/path/to/export.xml'


To delete a template:

nifi.templates.'GetUserData’.delete()


To get a map of processors (the map is name -> properties):

nifi.processors


To get a list of processor names:

nifi.processors*.key


To start a processor, fetch by name and call start:

nifi.processors.'Fetch User Data'.start()


The above also works for stop(), enable(), and disable()


To get the current state of a processor:

nifi.processors.'Fetch User Data'.state


To get the history:

nifi.controller.history


To get the last 10 provenance events:

nifi.controller.provenance.events(maxResults: 10)


To get the lineage for the latest provenance event:

nifi.controller.provenance.events(maxResults: 1)`[0`].lineage()


To get a process group called TestPG on the main canvas:

nifi.controller.root.groups.'TestPG'


To list the process groups under another process group (the parent named TestPG):

nifi.controller.root.groups.'TestPG'.groups.keySet()



This repo is very much a work in progress, and all contributions, comments, and suggestions are welcome!


License
------------------------------------

 nifi-client is copyright 2015- Matthew Burgess except where otherwise noted.

 This project is licensed under the Apache License Version 2.0 except where
 otherwise noted in the source files.
