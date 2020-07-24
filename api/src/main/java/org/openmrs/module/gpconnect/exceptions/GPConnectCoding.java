package org.openmrs.module.gpconnect.exceptions;

import org.hl7.fhir.dstu3.model.OperationOutcome.IssueType;

public enum GPConnectCoding {
    BAD_REQUEST("BAD_REQUEST", "Bad request", IssueType.INVALID),
    INVALID_PARAMETER("INVALID_PARAMETER", "Submitted parameter is not valid.", IssueType.INVALID),
    INVALID_RESOURCE("INVALID_RESOURCE", "Submitted resource is not valid.", IssueType.INVALID),
    INVALID_IDENTIFIER_SYSTEM("INVALID_IDENTIFIER_SYSTEM", "Invalid identifier system", IssueType.VALUE),
    INVALID_IDENTIFIER_VALUE("INVALID_IDENTIFIER_VALUE", "Invalid identifier value", IssueType.VALUE),
    INVALID_NHS_NUMBER("INVALID_NHS_NUMBER", "NHS number invalid", IssueType.VALUE),
    PATIENT_NOT_FOUND("PATIENT_NOT_FOUND", "Patient record not found", IssueType.NOTFOUND),
    PRACTITIONER_NOT_FOUND("PRACTITIONER_NOT_FOUND", "Practitioner record not found", IssueType.NOTFOUND),
    LOCATION_NOT_FOUND("LOCATION_NOT_FOUND", "Location record not found", IssueType.NOTFOUND),
    DUPLICATE_REJECTED("DUPLICATE_REJECTED", "Create would lead to creation of a duplicate resource", IssueType.DUPLICATE);

    private final String code;
    private final String display;
    private final IssueType issueType;

    GPConnectCoding(String code, String display, IssueType issueType) {
        this.code = code;
        this.display = display;
        this.issueType = issueType;
    }

    public String getCode() {
        return code;
    }

    public String getDisplay() {
        return display;
    }

    public IssueType getIssueType() {
        return issueType;
    }
}
