package servlets.data.execution.normal;

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

@WebServlet("/executeProgram")

public class ExecuteProgramServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException{
        String currentUser= ServletUserUtils.getUsernameFromCookies(req);
        if(currentUser==null){
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String progName = SelectedProgramManager.getSelectedProgram(currentUser);
        if (progName == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No selected program."));
            return;
        }

        EngineFacade engine = EngineFacadeManager.getEngine(currentUser, progName);
        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No engine registered for program."));
            return;
        }

        int degree = DegreeManager.getDegree(currentUser);
        String inputsCsv = req.getParameter("inputs");
        long[] inputs;

        if (inputsCsv == null || inputsCsv.isBlank()) {
            List<String> cached = engine.getCachedInputValues(degree);
            inputs = engine.prepareInputsFields(degree, cached);
        } else {
            inputs = engine.parseInputsCsv(inputsCsv, degree);
        }

        ExecutionReport report;
        try {
            report = engine.runWithReport(degree, inputs);
        } catch (Exception e) {
            ResponseWriter.write(res, JsonResponseUtils.error("Program execution failed: " + e.getMessage()));
            return;
        }

        if (report == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No execution report generated."));
            return;
        }

        UpdateFlagsManager.markUpdated("results");

        JsonObject response = JsonResponseUtils.success("Program executed successfully (degree " + degree + ").");
        response.add("report", new Gson().toJsonTree(report));
        response.add("finalVars", new Gson().toJsonTree(report.finalVars()));
        ResponseWriter.write(res, response);
    }
}
