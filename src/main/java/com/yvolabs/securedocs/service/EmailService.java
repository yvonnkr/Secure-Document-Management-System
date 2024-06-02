package com.yvolabs.securedocs.service;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 02/06/2024
 */

public interface EmailService {

    void sendNewAccountEmail(String name, String to, String token);

    void sendPasswordResetEmail(String name, String email, String token);
}
