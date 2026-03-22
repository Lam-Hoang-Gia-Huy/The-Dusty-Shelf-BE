package com.example.JWTImplemenation.Config;

public class UserContextHolder {
    private static final ThreadLocal<Integer> userHolder = new ThreadLocal<>();

    public static void setUserId(Integer userId) {
        userHolder.set(userId);
    }

    public static Integer getUserId() {
        return userHolder.get();
    }

    public static void clear() {
        userHolder.remove();
    }
}
