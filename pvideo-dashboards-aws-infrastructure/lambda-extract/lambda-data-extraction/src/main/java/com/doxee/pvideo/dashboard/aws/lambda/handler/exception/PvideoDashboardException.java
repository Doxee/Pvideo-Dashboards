package com.doxee.pvideo.dashboard.aws.lambda.handler.exception;

public class PvideoDashboardException extends RuntimeException {

    private static final long serialVersionUID = 2457851136559005811L;

    public PvideoDashboardException() {
        super();
    }

    public PvideoDashboardException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PvideoDashboardException(String message, Throwable cause) {
        super(message, cause);
    }

    public PvideoDashboardException(String message) {
        super(message);
    }

    public PvideoDashboardException(Throwable cause) {
        super(cause);
    }

}