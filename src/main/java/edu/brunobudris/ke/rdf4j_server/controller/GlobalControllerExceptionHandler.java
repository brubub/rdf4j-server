package edu.brunobudris.ke.rdf4j_server.controller;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType;
import edu.brunobudris.ke.rdf4j_server.model.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@Slf4j
@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    private static final Map<CommonControllerExceptionType, HttpStatus> EXCEPTION_TYPE_TO_STATUS_MAP =
            Map.of(
                    CommonControllerExceptionType.NOT_FOUND, NOT_FOUND,
                    CommonControllerExceptionType.NOT_ACCEPTABLE, NOT_ACCEPTABLE,
                    CommonControllerExceptionType.CONFLICT, CONFLICT,
                    CommonControllerExceptionType.UNSUPPORTED_MEDIA_TYPE, UNSUPPORTED_MEDIA_TYPE,
                    CommonControllerExceptionType.INTERNAL_ERROR, INTERNAL_SERVER_ERROR,
                    CommonControllerExceptionType.BAD_REQUEST, BAD_REQUEST,
                    CommonControllerExceptionType.UNPROCESSABLE_ENTITY, UNPROCESSABLE_ENTITY
            );

    @ExceptionHandler(value = CommonControllerException.class, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponse> commonControllerExceptionHandlerAsJSON(CommonControllerException ex) {
        log.error("An exception was intercepted", ex);

        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setWarning(ex.getMessage());

        HttpStatus httpStatus = EXCEPTION_TYPE_TO_STATUS_MAP.get(ex.getType());

        return ResponseEntity
                .status(httpStatus)
                .body(baseResponse);
    }

    @ExceptionHandler(value = CommonControllerException.class, produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> commonControllerExceptionHandlerAsTextPlain(CommonControllerException ex) {
        log.error("An exception was intercepted", ex);

        HttpStatus httpStatus = EXCEPTION_TYPE_TO_STATUS_MAP.get(ex.getType());

        return ResponseEntity
                .status(httpStatus)
                .body(ex.getMessage());
    }
}
