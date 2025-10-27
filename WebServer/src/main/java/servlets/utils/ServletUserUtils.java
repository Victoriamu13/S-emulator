package servlets.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

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

    public static boolean isUserLoggedIn(HttpServletRequest req) {
        return getUsernameFromCookies(req) != null;
    }

}
