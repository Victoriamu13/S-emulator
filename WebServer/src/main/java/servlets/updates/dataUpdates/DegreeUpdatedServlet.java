package servlets.updates.dataUpdates;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/degreeUpdated")

public class DegreeUpdatedServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{
        JsonObject response=new JsonObject();
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            response.addProperty("updated", false);
            ResponseWriter.write(res, response);
            return;
        }

        boolean updated = UpdateFlagsManager.hasUpdated("degree", username);
        response.addProperty("updated", updated);

        if (updated) UpdateFlagsManager.clearFlag("degree", username);
        ResponseWriter.write(res, response);
    }
}
