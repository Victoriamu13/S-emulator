package servlets.data.execution.debug;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.ExecutionReport;
import logic.system.data.expansion.DegreeManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.engine.EngineFacadeManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;
import java.util.List;

@WebServlet("/startDebug")

public class StartDebugServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException{
        String username= ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String progName = SelectedProgramManager.getSelectedProgram(username);
        if (progName == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active program selection."));
            return;
        }

        EngineFacade engine = EngineFacadeManager.getEngine(username, progName);
        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active engine for this program."));
            return;
        }

        int degree = DegreeManager.getDegree(username,progName);
        String csv = req.getParameter("inputs");
        long[] inputs;
        if (csv == null || csv.isBlank()) {
            List<String> cached = engine.getCachedInputValues(degree);
            inputs = engine.prepareInputsFields(degree, cached);
        } else {
            inputs = engine.parseInputsCsv(csv, degree);
        }

        engine.startDebugSession(degree, inputs);
        ExecutionReport initial = engine.buildInitialReport();

        UpdateFlagsManager.markUpdated("debugResults");

        JsonObject response = JsonResponseUtils.success("Debug started.");
        response.add("report", new Gson().toJsonTree(initial));
        ResponseWriter.write(res, response);
    }
}
