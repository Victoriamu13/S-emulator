package servlets.updates.userUpdates;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/usersUpdated")

public class UserUpdatedServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonObject response=new JsonObject();

        boolean updated = UpdateFlagsManager.hasUpdated("users");
        response.addProperty("updated", updated);

        if (updated) {
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            UpdateFlagsManager.clearFlag("users");
        }
        ResponseWriter.write(res, response);
    }
}
