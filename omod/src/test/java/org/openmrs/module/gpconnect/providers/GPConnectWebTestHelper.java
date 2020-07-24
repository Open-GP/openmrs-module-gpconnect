package org.openmrs.module.gpconnect.providers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueType;
import org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.openmrs.module.gpconnect.util.CodeSystems;

public class GPConnectWebTestHelper {
    public static void assertThatOperationOutcomeHasCorrectStructureAndContent(OperationOutcome operationOutcome, String theCode, String theDisplay, IssueType issueType, String errorMessage) {
        assertTrue(operationOutcome.hasMeta());

        List<OperationOutcomeIssueComponent> issues = operationOutcome.getIssue();
        assertThat(issues.size(), equalTo(1));

        OperationOutcomeIssueComponent issue = operationOutcome.getIssue().get(0);

        assertThat(issue.getSeverity(), equalTo(OperationOutcome.IssueSeverity.ERROR));
        assertThat(issue.getCode(), equalTo(issueType));

        Coding expectedCoding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, theCode, theDisplay);
        List<Coding> coding = issue.getDetails().getCoding();
        assertThat(coding.size(), equalTo(1));
        assertThat(expectedCoding.getCode(), equalTo(coding.get(0).getCode()));
        assertThat(expectedCoding.getDisplay(), equalTo(coding.get(0).getDisplay()));

        assertThat(issue.getDiagnostics(), equalTo(errorMessage));
    }
}

