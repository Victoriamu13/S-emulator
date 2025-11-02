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

@WebServlet("/markHistoryUpdated")
public class MarkHistoryUpdatedServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonObject response = new JsonObject();

        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            response.addProperty("updated", false);
            ResponseWriter.write(res, response);
            return;
        }
        UpdateFlagsManager.markUpdated("history");

        response.addProperty("state", "SUCCESS");
        response.addProperty("message", "History update flag raised successfully.");

        ResponseWriter.write(res, response);
    }
}
