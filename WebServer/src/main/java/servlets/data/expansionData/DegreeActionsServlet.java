package servlets.data.expansionData;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.data.expansion.DegreeManager;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/degreeAction")

public class DegreeActionsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String currentUser = ServletUserUtils.getUsernameFromCookies(req);

        if (currentUser == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }
        int newDegree = 0;
        try {
            newDegree = Integer.parseInt(req.getParameter("degree"));

        } catch (Exception ignored) {
        }

        DegreeManager.setDegree(currentUser, newDegree);
        UpdateFlagsManager.markUpdated("degree");

        JsonObject response = JsonResponseUtils.success("Degree updated successfully.");
        response.addProperty("degree", newDegree);
        ResponseWriter.write(res, response);

    }
}
