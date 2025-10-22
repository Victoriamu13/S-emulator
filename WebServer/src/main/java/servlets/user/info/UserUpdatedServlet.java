package servlets.user.info;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.ResponseWriter;

import java.io.IOException;

@WebServlet("/usersUpdated")

public class UserUpdatedServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        boolean updated= UpdateFlagsManager.hasUpdated("users");

        JsonObject response=new JsonObject();
        response.addProperty("updated", updated);

        ResponseWriter.write(res,response);
    }
}
