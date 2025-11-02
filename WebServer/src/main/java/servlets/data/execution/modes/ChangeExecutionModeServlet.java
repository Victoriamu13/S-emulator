package servlets.data.execution.modes;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.api.EngineFacade;
import logic.system.data.execution.execMode.ExecutionModeManager;
import logic.system.data.highlight.DebugHighlightManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.engine.EngineFacadeManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/changeExecutionMode")
public class ChangeExecutionModeServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String mode = req.getParameter("mode");
        if (mode == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Mode not specified."));
            return;
        }

        String program = SelectedProgramManager.getSelectedProgram(username);
        EngineFacade engine = EngineFacadeManager.getEngine(username, program);

        // Prevent mode change if debug session is active
        if (engine != null && engine.isDebugActive()) {
            ResponseWriter.write(res, JsonResponseUtils.error(
                    "Cannot change execution mode during an active debug session."
            ));
            return;
        }

        ExecutionModeManager.setMode(username,mode);
        UpdateFlagsManager.markUpdated("initExecution",username);

        JsonObject response = JsonResponseUtils.success("Execution mode set to " + mode);
        response.addProperty("mode", mode);
        ResponseWriter.write(res, response);
    }
}
