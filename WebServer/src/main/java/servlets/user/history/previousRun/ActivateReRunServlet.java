package servlets.user.history.previousRun;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.api.EngineFacade;
import logic.system.data.execution.runHistory.ReRunStateManager;
import logic.system.data.expansion.DegreeManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.user.engine.EngineFacadeManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/activateReRun")
public class ActivateReRunServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String progName = req.getParameter("progName");
        String progType = req.getParameter("progType");
        String degreeStr = req.getParameter("degree");

        if (progName == null || progType == null || degreeStr == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Missing parameters for ReRun activation."));
            return;
        }

        int degree;
        try {
            degree = Integer.parseInt(degreeStr);
        } catch (NumberFormatException e) {
            ResponseWriter.write(res, JsonResponseUtils.error("Invalid degree parameter."));
            return;
        }

        ReRunStateManager.setReRun(username, true);
        DegreeManager.setDegree(username,progName, degree);
        SelectedProgramManager.setSelectedProgram(username, progType, progName);

        EngineFacade engine = EngineFacadeManager.getEngine(username, progName);
        if (engine == null) {
            engine = EngineFacadeManager.getEngine(username, progName);

            if (engine == null) {
                ResponseWriter.write(res, JsonResponseUtils.error("Failed to create engine for ReRun program."));
                return;
            }
        }

        JsonObject response = JsonResponseUtils.success("ReRun mode activated successfully.");
        ResponseWriter.write(res, response);
    }

}
