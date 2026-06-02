package edu.brunobudris.ke.rdf4j_server.exception;

import lombok.Getter;

@Getter
public class CommonControllerException extends RuntimeException {

    private final CommonControllerExceptionType type;

    public CommonControllerException(CommonControllerExceptionType type, String message) {
        super(message);
        this.type = type;
    }
}
