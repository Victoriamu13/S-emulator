package servlets.login;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.api.SystemManager;
import logic.system.api.SystemManagerImpl;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/login")

public class LoginServlet extends HttpServlet {

    private final SystemManager systemManager=new SystemManagerImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)throws IOException {
        JsonObject response;
        String username = req.getParameter("user");

        if (username == null || username.trim().isEmpty()) {
            response = JsonResponseUtils.error("Username cannot be empty.");
        }
        else if(systemManager.userExists(username)){
            response = JsonResponseUtils.error("Username already exists. Please choose another one.");
        }
        else {
            systemManager.addUser(username);
            systemManager.addCredits(username, 0);
            UpdateFlagsManager.markUpdated("users");

            Cookie cookie = new Cookie("username", username);
            cookie.setPath("/");
            res.addCookie(cookie);

            response = JsonResponseUtils.success("Login successful.");
        }

        ResponseWriter.write(res, response);
    }
}
