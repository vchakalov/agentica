package com.agentica.workflows.validator;

import java.util.List;

/**
 * Exception thrown when workflow plan validation fails.
 */
public class WorkflowValidationException extends RuntimeException {

    private final String planId;

    private final List<String> errors;

    public WorkflowValidationException(String planId, List<String> errors) {

        super(buildMessage(planId, errors));

        this.planId = planId;
        this.errors = List.copyOf(errors);
    }

    public String getPlanId() {

        return planId;
    }

    public List<String> getErrors() {

        return errors;
    }

    private static String buildMessage(String planId, List<String> errors) {

        return "Workflow plan validation failed, planId: " + planId +
            ", errors: [" + String.join("; ", errors) + "]";
    }

}
