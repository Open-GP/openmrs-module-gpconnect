GPConnect module
==========

The aim of this module is to enhance the existing FHIR OpenMRS module to be GPConnect compatible - see more details about what GPConnect is [here](https://digital.nhs.uk/services/gp-connect). 

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

 