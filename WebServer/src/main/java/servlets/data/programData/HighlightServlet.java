package servlets.data.programData;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.data.highlight.HighlightManager;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/highlightVariable")
public class HighlightServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String user = ServletUserUtils.getUsernameFromCookies(req);
        if (user == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active session."));
            return;
        }

        String var = req.getParameter("variable");
        if (var == null || var.isBlank()) {
            HighlightManager.clearHighlight(user);
        } else {
            HighlightManager.setHighlight(user, var);
        }

        UpdateFlagsManager.markUpdated("highlight");
        ResponseWriter.write(res, JsonResponseUtils.success("Highlight updated."));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String user = ServletUserUtils.getUsernameFromCookies(req);
        if (user == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active session."));
            return;
        }

        String highlight = HighlightManager.getHighlight(user);
        JsonObject response = JsonResponseUtils.success("Highlight fetched.");
        response.addProperty("highlight", highlight);
        ResponseWriter.write(res, response);
    }
}