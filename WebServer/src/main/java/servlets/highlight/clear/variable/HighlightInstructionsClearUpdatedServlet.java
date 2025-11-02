package servlets.highlight.clear.variable;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/highlightInstructionsClearUpdated")
public class HighlightInstructionsClearUpdatedServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonObject response = new JsonObject();
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active session."));
            return;
        }

        boolean updated = UpdateFlagsManager.hasUpdated("highlightInstructionsClear",username);
        response.addProperty("updated", updated);
        if (updated) {
            UpdateFlagsManager.clearFlag("highlightInstructionsClear",username);
        }
        ResponseWriter.write(res, response);
    }
}