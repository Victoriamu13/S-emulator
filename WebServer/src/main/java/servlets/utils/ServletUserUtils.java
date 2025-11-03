package servlets.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletUserUtils {
    private ServletUserUtils() {}

    public static String getUsernameFromCookies(HttpServletRequest req) {
        if (req == null || req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if ("username".equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    public static void clearUserCookie(HttpServletResponse res) {
        Cookie cookie = new Cookie("username", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        res.addCookie(cookie);
    }
}
