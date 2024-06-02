package com.yvolabs.securedocs.exception;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 01/06/2024
 */
public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }

    public ApiException(){
        super("An error occurred");
    }


}
