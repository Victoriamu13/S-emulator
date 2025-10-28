package servlets.data.execution.modes;


import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;

import java.io.IOException;

@WebServlet("/changeMode")
public class ChangeModeServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String mode = req.getParameter("mode"); // NORMAL / DEBUG

        if (mode == null || mode.isBlank()) {
            ResponseWriter.write(res, JsonResponseUtils.error("Missing mode parameter."));
            return;
        }

        UpdateFlagsManager.markUpdated("initExecution");

        JsonObject response = JsonResponseUtils.success("Mode changed to " + mode);
        response.addProperty("mode", mode);
        ResponseWriter.write(res, response);
    }
}
