package com.codercultrera.FilmFinder_Backend.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtils {

    public static void setCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true); // Use true in production; false in development
        cookie.setSecure(true); // Use true in production
        cookie.setPath("/");
        response.addCookie(cookie);

        // Manually add the SameSite=None attribute to the Set-Cookie header
        String cookieHeader = "Set-Cookie=" + cookie.getName() + "=" + cookie.getValue() +
                "; Path=" + cookie.getPath() +
                "; HttpOnly; Secure; SameSite=None";
        response.addHeader("Set-Cookie", cookieHeader);

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
