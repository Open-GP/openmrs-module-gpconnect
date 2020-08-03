package org.openmrs.module.gpconnect.services;

import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.dstu3.model.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.gpconnect.util.Extensions;

import java.util.Collections;
import java.util.Date;

import static org.openmrs.module.gpconnect.GPConnectTestHelper.assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome;

@RunWith(MockitoJUnitRunner.class)
public class PatientRegistrationValidatorTest {
    private static final String INVALID_NHS_NUMBER = "123456789";
    private static final String VALID_NHS_NUMBER = "1234567890";

    private Patient validPatient;

    @Before
    public void setup() {
        validPatient = getValidGPConnectPatient(VALID_NHS_NUMBER);
    }

    @Test
    public void shouldSuccessfullyValidatePatient() {
        PatientRegistrationValidator.validate(validPatient, true);
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithNoNhsNumber() {
        validPatient = getValidGPConnectPatient("");
        validPatient.setIdentifier(Collections.emptyList());

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), InvalidRequestException.class,
                "INVALID_NHS_NUMBER", "NHS number invalid", OperationOutcome.IssueType.VALUE, "Patient is missing id");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithInvalidNhsNumber() {
        validPatient = getValidGPConnectPatient(INVALID_NHS_NUMBER);
        validPatient.setIdentifier(Collections.emptyList());

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), InvalidRequestException.class,
                "INVALID_NHS_NUMBER", "NHS number invalid", OperationOutcome.IssueType.VALUE, "Patient is missing id");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringIdentifierWithoutSystem() {
        validPatient.getIdentifier().get(0).setSystem(null);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), InvalidRequestException.class,
                "BAD_REQUEST", "Bad request", OperationOutcome.IssueType.INVALID, "Identifier is missing System");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringMultipleIdentifiersWithoutSystem() {
        Identifier identifier = new Identifier();
        identifier.setValue("No system");
        validPatient.addIdentifier(identifier);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), InvalidRequestException.class,
                "BAD_REQUEST", "Bad request", OperationOutcome.IssueType.INVALID, "Identifier is missing System");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithAnimal() {
        Patient.AnimalComponent animal = new Patient.AnimalComponent();
        animal.setId("dog");
        validPatient.setAnimal(animal);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), UnprocessableEntityException.class,
                "INVALID_RESOURCE", "Submitted resource is not valid.", OperationOutcome.IssueType.INVALID, "Not allowed field: Animal");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithActivePatient() {
        validPatient.setActive(true);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), UnprocessableEntityException.class,
                "INVALID_RESOURCE", "Submitted resource is not valid.", OperationOutcome.IssueType.INVALID, "Not allowed field: Active");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithMultipleBirths() {
        validPatient.setMultipleBirth(new BooleanType().setValue(true));

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), UnprocessableEntityException.class,
                "INVALID_RESOURCE", "Submitted resource is not valid.", OperationOutcome.IssueType.INVALID, "Not allowed field: Multiple Births");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithCareProvider() {
        Reference reference = new Reference();
        reference.setDisplay("Test reference");
        validPatient.addGeneralPractitioner(reference);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), UnprocessableEntityException.class,
                "INVALID_RESOURCE", "Submitted resource is not valid.", OperationOutcome.IssueType.INVALID, "Not allowed field: General Practitioner");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithContact() {
        Patient.ContactComponent contactComponent = new Patient.ContactComponent();
        HumanName name = new HumanName();
        name.setFamily("Smith");
        contactComponent.setName(name);

        validPatient.addContact(contactComponent);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), UnprocessableEntityException.class,
                "INVALID_RESOURCE", "Submitted resource is not valid.", OperationOutcome.IssueType.INVALID, "Not allowed field: Contact");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithManagingOrganization() {
        Reference managingOrgReference = new Reference();
        managingOrgReference.setDisplay("Managing Organisation");

        validPatient.setManagingOrganization(managingOrgReference);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), UnprocessableEntityException.class,
                "INVALID_RESOURCE", "Submitted resource is not valid.", OperationOutcome.IssueType.INVALID, "Not allowed field: Managing Organisation");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithMaritalStatus() {
        Coding coding = new Coding();
        CodeableConcept codeableConcept = new CodeableConcept();
        coding.setSystem("http://hl7.org/fhir/marital-status");
        coding.setCode("M");
        codeableConcept.addCoding(coding);

        validPatient.setMaritalStatus(codeableConcept);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), UnprocessableEntityException.class,
                "INVALID_RESOURCE", "Submitted resource is not valid.", OperationOutcome.IssueType.INVALID, "Not allowed field: Marital Status");
    }


    @Test
    public void shouldThrowExceptionWhenRegisteringWithCommunication() {
        Patient.PatientCommunicationComponent communication = new Patient.PatientCommunicationComponent();
        communication.setId("some communication");
        validPatient.setCommunication(Collections.singletonList(communication));

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), UnprocessableEntityException.class,
                "INVALID_RESOURCE", "Submitted resource is not valid.", OperationOutcome.IssueType.INVALID, "Not allowed field: Communication");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithPhoto() {
        Attachment attachment = new Attachment();
        attachment.setId("Some photo");
        validPatient.setPhoto(Collections.singletonList(attachment));

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), UnprocessableEntityException.class,
                "INVALID_RESOURCE", "Submitted resource is not valid.", OperationOutcome.IssueType.INVALID, "Not allowed field: Photo");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithInvalidTelecom() {
        addPatientTelecom(validPatient, ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.TEMP, "07923456789");

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), InvalidRequestException.class,
                "BAD_REQUEST", "Bad request", OperationOutcome.IssueType.INVALID, "Invalid telecom. Duplicate use of: {System: Phone, Use: Temp}");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithMultipleInvalidTelecom() {
        addPatientTelecom(validPatient, ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.TEMP, "07923456789");
        addPatientTelecom(validPatient, ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.HOME, "07923456781");
        addPatientTelecom(validPatient, ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.HOME, "07923456782");

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), InvalidRequestException.class,
                "BAD_REQUEST", "Bad request", OperationOutcome.IssueType.INVALID, "Invalid telecom. Duplicate use of: {System: Phone, Use: Home}, {System: Phone, Use: Temp}");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithDeceased() {
        validPatient.setDeceased(new BooleanType(true));

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), UnprocessableEntityException.class,
                "INVALID_RESOURCE", "Submitted resource is not valid.", OperationOutcome.IssueType.INVALID, "Not allowed field: Deceased");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithoutDob() {
        validPatient.setBirthDate(null);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), InvalidRequestException.class,
                "BAD_REQUEST", "Bad request", OperationOutcome.IssueType.INVALID, "Birth date is mandatory");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithoutOfficialName() {
        validPatient.setName(Collections.emptyList());

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), InvalidRequestException.class,
                "BAD_REQUEST", "Bad request", OperationOutcome.IssueType.INVALID, "Patient must have an official name containing at least a family name");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithoutFamilyName() {
        HumanName homer = new HumanName().setUse(HumanName.NameUse.OFFICIAL)
                .setGiven(Collections.singletonList(new StringType("Homer")));
        validPatient.setName(Collections.singletonList(homer));

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> PatientRegistrationValidator.validate(validPatient, true), InvalidRequestException.class,
                "BAD_REQUEST", "Bad request", OperationOutcome.IssueType.INVALID, "Patient must have an official name containing at least a family name");
    }

    private void addPatientTelecom(Patient validPatient, ContactPoint.ContactPointSystem contactPointSystem, ContactPoint.ContactPointUse contactPointUse, String contactValue) {
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(contactPointSystem);
        contactPoint.setValue(contactValue);
        contactPoint.setUse(contactPointUse);
        validPatient.addTelecom(contactPoint);
    }

    private Patient getValidGPConnectPatient(String nhsNumber) {
        validPatient = new Patient();
        validPatient.setBirthDate(new Date());

        HumanName humanName = new HumanName();
        humanName.setGiven(Collections.singletonList(new StringType("Homer")));
        humanName.setFamily("Simpson");
        humanName.setUse(HumanName.NameUse.OFFICIAL);
        validPatient.setName(Collections.singletonList(humanName));

        Identifier identifier = new Identifier();
        identifier.setValue(nhsNumber);
        identifier.setSystem(Extensions.NHS_NUMBER_SYSTEM);
        validPatient.addIdentifier(identifier);

        addPatientTelecom(validPatient, ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.TEMP, "07123446789");
        addPatientTelecom(validPatient, ContactPoint.ContactPointSystem.EMAIL, ContactPoint.ContactPointUse.TEMP, "hello@temp.com");

        return validPatient;
    }
}
