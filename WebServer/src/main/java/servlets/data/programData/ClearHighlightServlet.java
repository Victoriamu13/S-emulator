package servlets.data.programData;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import java.io.IOException;

@WebServlet("/clearHighlight")
public class ClearHighlightServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        UpdateFlagsManager.clearFlag("highlight");
        ResponseWriter.write(res, JsonResponseUtils.success("Highlight flag cleared."));
    }
}
