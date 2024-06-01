package com.yvolabs.securedocs.domain;

/**
 * @author Yvonne N
 * @version 1.0
 * @since 01/06/2024
 *
 * @apiNote Will use this class to set/get the value of entity that require field of type ThreadLocal
 */

public class RequestContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private RequestContext() {

    }

    public static void start() {
        USER_ID.remove();
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }
}
