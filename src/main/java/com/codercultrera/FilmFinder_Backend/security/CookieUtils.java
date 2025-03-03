package com.codercultrera.FilmFinder_Backend.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtils {

    // This is the old way of setting cookies. I have since updated this in
    // AuthService and JwtAuthFilter.
    public static void setCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true); // Use true in production; false in development
        cookie.setSecure(true); // Use true in production
        cookie.setPath("/");
        response.addCookie(cookie);

        String cookieHeader = String.format("%s; %s; Path=%s; HttpOnly; Secure; SameSite=None",
                name, value, cookie.getPath());
        response.setHeader("Set-Cookie", cookieHeader);
    }

    public static String getTokenFromCookie(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
