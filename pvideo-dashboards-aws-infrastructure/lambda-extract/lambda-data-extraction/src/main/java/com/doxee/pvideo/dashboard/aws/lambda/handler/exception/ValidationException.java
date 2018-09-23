package com.doxee.pvideo.dashboard.aws.lambda.handler.exception;

import java.util.List;

public class ValidationException extends Exception {

    private static final long serialVersionUID = 1L;

    private final List<String> invalidFields;

    /**
     * Constructor
     * 
     * @param message
     *            that is the custom message that is thrown with the exception.
     */
    public ValidationException(String message, List<String> invalidFields) {
        super(message);
        this.invalidFields = invalidFields;
    }

    /**
     * This method returns the list of invalid fields found during the validation.
     * 
     * @return invalidFields that is a list of strings
     */
    public List<String> getInvalidFields() {
        return invalidFields;
    }

}