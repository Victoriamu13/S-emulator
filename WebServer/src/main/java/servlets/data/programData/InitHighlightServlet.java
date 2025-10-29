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

@WebServlet("/initHighlight")
public class InitHighlightServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String user = ServletUserUtils.getUsernameFromCookies(req);

        if (user == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        HighlightManager.clearHighlight(user);
        UpdateFlagsManager.markUpdated("highlight");

        JsonObject response = JsonResponseUtils.success("Highlight cleared.");
        ResponseWriter.write(res, response);
    }
}
