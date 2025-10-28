package servlets.data.execution.debug;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.ExecutionReport;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.engine.EngineFacadeManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/resumeDebug")
public class ResumeDebugServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String program = SelectedProgramManager.getSelectedProgram(username);
        EngineFacade engine = EngineFacadeManager.getEngine(username, program);
        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active engine found."));
            return;
        }

        ExecutionReport report = engine.resume();
        if (report == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Debug session not active."));
            return;
        }

        UpdateFlagsManager.markUpdated("debugResults");

        JsonObject response = JsonResponseUtils.success("Program resumed to completion.");
        response.add("report", new Gson().toJsonTree(report));
        ResponseWriter.write(res, response);
    }
}
