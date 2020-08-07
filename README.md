GPConnect module
================

The aim of this module is to enhance the existing FHIR OpenMRS module to be GPConnect compatible - see more details about what GPConnect is [here](https://digital.nhs.uk/services/gp-connect). Further resources can also be found [here](https://github.com/Open-GP/openmrs-module-gpconnect/wiki/General-Resources)

A high level specification of the API can be found here: https://digital.nhs.uk/services/gp-connect/gp-connect-specifications-for-developers. Technically speaking, GPConnect is a standard extending [FHIR STU3](https://www.hl7.org/fhir/stu3/) - to note that is **not** the latest version of the FHIR standard. 

Currently there are 2 FHIR OpenMRS modules:
* https://github.com/openmrs/openmrs-module-fhir2 - which currently under development and implements [FHIR R4](https://hl7.org/fhir/R4/) which is the latest FHIR version
* https://github.com/openmrs/openmrs-module-fhir - which is part of the vanilla OpenMRS reference application (ie. implementation finished) and implements [FHIR STU3](https://www.hl7.org/fhir/stu3/) (the version GPConnect is based on)

This module relies on [openmrs-module-fhir](https://github.com/openmrs/openmrs-module-fhir) 

A technical specification of GPConnect API can be found here: http://orange.testlab.nhs.uk/. It contains Swagger docs, a Postman collection and a simple test webapp using the test deployment of GPConnect. 

As part of this modules we would implement the extension of FHIR resources needed to match the GPConnect API. These resources are:
* Patient
* Organization
* Practitioner
* Location
* Appointment

For each of the resources above there is an associated GPConnect FHIR profile found here: https://fhir.nhs.uk/StructureDefinition 

Setup
=====

* Check Java version installed - it should be Java 8 or higher:
    ```
    $ java --version
    ```
    If you need to install Java, then you will need to install [OpenJDK](http://jdk.java.net/).


* Check that you have the latest version of Apache Maven installed:
    ```
    $ mvn --version
    ```
    [Download Apache Maven](https://maven.apache.org/download.cgi)
    
    [Install Apache Maven](https://maven.apache.org/install.html)
       

* Install [Postman](https://www.postman.com/downloads/) for testing the endpoints of this API.

* To build the Maven project:
    ```
    $ mvn clean install
    ```
  
* To run the tests of the Maven project:
    ```
    $ mvn test
    ```

### Development cycle
#### Development
- Practising trunk-based development (using branches only for experiments and end of day work saving)
- Using TDD as much as possible (note: there needs to be some work on improving testing coverage around integration between components)
- Before pushing to master:
   - Run all java tests
   - Deploy OpenGP distro locally with the latest version of the GPConnect module
   - Run focused feature tests from gpconnect-provider-testing - and make sure all that need to pass are passing

#### QA checklist
- Run all (unit + gpconnect-provider-testing) tests and check passing

- Compare the feature file in FocusedTests with the main feature file to see what has been commented out, and if any of this can now be uncommented and fixed as part of the ticket or whether the approach needs discussing, are they known issues etc

    - If there are cases that can be fixed as part of this ticket do this next

- Check the app locally to check functionality against ACs

    - Make sure the data in the DB is set up 

- Look at the code itself to understand the quality of what is written (to get accustomed with the codebase)

- Look at test coverage

- Do some exploratory testing locally

    * Try and break things

    * Make sure you’re connected to the MySQL database (username and password in the dockers compose file)

    * Can be used to reproduce a certain test case as in openMRS

- Add any remaining test coverage eg. The high level HTTP req unit tests in the java module


  

### Running OpenGP with only GPConnect

Running the OpenGP distro takes 5+ mins making it hard to get quick feedback for changes to the GPConnect module. As an alternative, you could setup a bare version of OpenMRS with only the core and the GPConnect module (see the dev-server-distro.properties file)

The following instruction will run the the OpenMRS server locally (not on Docker) and use a docker container for the MySQL database

* Setting up the server: 
```shell script
./create.sh
``` 
Notes: this creates an OpenMRS server with the name `dev-server`; you will be asked to choose ports where it runs - use: `8081` and `1045` for debugging

* Running the server:
```shell script
./run.sh
```
Notes: this compiles the GPConnect module and starts the dev-server with the new code

* Setting up the test data:
```shell script
PASS=<password> ./setupData.sh
```
Notes: this assumes the port chosen in the setting up step is `8081` and that the gpconnect-provider-testing repo is at the same level as this repo
