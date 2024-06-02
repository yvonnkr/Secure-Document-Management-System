package com.yvolabs.securedocs.utils;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 02/06/2024
 */


public class EmailUtils {

    public static String getEmailMessage(String name, String host, String token){
        return "Hello " + name + ",\n\nYour new account has been created. Please click on the new link below to verify your account.\n\n " +
                getVerificationUrl(host, token) + "\n\nThe Support Team";
    }

    public static String getResetPasswordMessage(String name, String host, String token){
        return "Hello " + name + ",\n\nYour new account has been created. Please click on the new link below to verify your account.\n\n " +
                getResetPasswordUrl(host, token) + "\n\nThe Support Team";
    }

    private static String getVerificationUrl(String host, String token) {
        return host + "/verify/account?token=" + token;
    }

    private static String getResetPasswordUrl(String host, String token) {
        return host + "/verify/password?token=" + token;
    }
}
