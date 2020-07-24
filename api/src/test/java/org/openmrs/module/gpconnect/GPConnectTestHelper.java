package org.openmrs.module.gpconnect;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import java.util.List;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueType;
import org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.openmrs.module.gpconnect.util.CodeSystems;

public class GPConnectTestHelper {
    public static TokenAndListParam generateIdentifier(String system, String value) {
        TokenParam tokenParam = new TokenParam(system, value);
        return new TokenAndListParam().addAnd(tokenParam);
    }

    public static void assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(Runnable action, Class exceptionClass, String codingCode, String codingDisplay, IssueType issueType, String diagnostics) {
        try {
            action.run();
            fail("Exception expected to be thrown but wasn't");
        } catch (final BaseServerResponseException exception) {
            assertTrue(exceptionClass.isInstance(exception));
            final OperationOutcome operationOutcome = (OperationOutcome) exception.getOperationOutcome();
            assertThatOperationOutcomeHasCorrectStructureAndContent(
                operationOutcome, codingCode, codingDisplay, issueType, diagnostics
            );
        }
    }

    public static void assertThatOperationOutcomeHasCorrectStructureAndContent(OperationOutcome operationOutcome, String theCode, String theDisplay, IssueType issueType, String diagnostics) {
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

        assertThat(issue.getDiagnostics(), equalTo(diagnostics));
    }
}
