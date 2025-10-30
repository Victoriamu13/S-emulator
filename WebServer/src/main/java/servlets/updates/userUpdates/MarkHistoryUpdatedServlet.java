package servlets.updates.userUpdates;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.ResponseWriter;

import java.io.IOException;

@WebServlet("/markHistoryUpdated")
public class MarkHistoryUpdatedServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        UpdateFlagsManager.markUpdated("history");

        JsonObject obj = new JsonObject();
        obj.addProperty("state", "SUCCESS");
        obj.addProperty("message", "History update flag raised successfully.");

        ResponseWriter.write(res, obj);
    }
}
