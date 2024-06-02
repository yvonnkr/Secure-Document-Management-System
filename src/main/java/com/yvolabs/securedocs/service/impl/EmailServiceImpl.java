package com.yvolabs.securedocs.service.impl;

import com.yvolabs.securedocs.exception.ApiException;
import com.yvolabs.securedocs.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.yvolabs.securedocs.utils.EmailUtils.getEmailMessage;
import static com.yvolabs.securedocs.utils.EmailUtils.getResetPasswordMessage;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 02/06/2024
 *
 * @apiNote Async Methods will run on a separate thread, so the execution of the Main thread will continue without waiting for emails send request to complete
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private static final String NEW_USER_ACCOUNT_VERIFICATION = "New User Account Verification";
    private static final String PASSWORD_RESET_REQUEST = "Reset Password Request";

    private final JavaMailSender sender;

    @Value("${spring.mail.verify.host}")
    private String host;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendNewAccountEmail(String name, String email, String token) {
        try {
            var message = new SimpleMailMessage();
            message.setSubject(NEW_USER_ACCOUNT_VERIFICATION);
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setText(getEmailMessage(name, host, token));

            sender.send(message);

        } catch (Exception ex) {
            log.error(ex.getMessage());

            // Ideally its good practice not to throw error when trying to send an email, would let the user report if they didn't receive, but for learning purpose we will throw
            // Also not a good idea to pass the exact error msg as may contain some sensitive data, instead use some simple informative msg like below
            throw new ApiException("Unable to send email");
        }

    }

    @Override
    @Async
    public void sendPasswordResetEmail(String name, String email, String token) {

        try {
            var message = new SimpleMailMessage();
            message.setSubject(PASSWORD_RESET_REQUEST);
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setText(getResetPasswordMessage(name, host, token));

            sender.send(message);

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new ApiException("Unable to send email");

        }
    }
}
