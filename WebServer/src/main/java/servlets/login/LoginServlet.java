package servlets.login;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/login")

public class LoginServlet extends HttpServlet {
    private static final Set<String> activeUsers= ConcurrentHashMap.newKeySet();
    private final Gson gson=new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException {
        JsonObject response;
        String username = req.getParameter("user");

        if (username == null || username.trim().isEmpty()) {
            response = JsonResponseUtils.error("Username cannot be empty.");
        }
        else if(activeUsers.contains(username)){
            response = JsonResponseUtils.error("Username already exists. Please choose another one.");        }
        else
        {
            activeUsers.add(username);

            //Create username cookie
            Cookie cookie=new Cookie("username",username);
            cookie.setPath("/");
            res.addCookie(cookie);

            response = JsonResponseUtils.success("Login successful.");

        }
        ResponseWriter.write(res, response);
    }
}
