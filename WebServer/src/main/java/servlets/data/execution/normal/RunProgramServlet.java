package servlets.data.execution.normal;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.ExecutionReport;
import logic.engineFacade.model.RunRecord;
import logic.system.data.architecture.ArchitectureManager;
import logic.system.data.execution.runHistory.ReRunStateManager;
import logic.system.data.expansion.DegreeManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.engine.EngineFacadeManager;
import logic.system.user.history.userHstory.UserHistory;
import logic.system.user.history.userHstory.UserHistoryManager;
import logic.system.user.info.UserInfoManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebServlet("/runProgram")

public class RunProgramServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException{
        String username= ServletUserUtils.getUsernameFromCookies(req);
        if(username==null){
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String progName = SelectedProgramManager.getSelectedProgram(username);
        if (progName == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No selected program."));
            return;
        }

        EngineFacade engine = EngineFacadeManager.getEngine(username, progName);
        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No engine registered for program."));
            return;
        }

        int degree = DegreeManager.getDegree(username,progName);
        String inputsCsv = req.getParameter("inputs");

        long[] inputs;

        // Load existing or cached inputs
        if (inputsCsv == null || inputsCsv.isBlank()) {
            List<String> cached = engine.getCachedInputValues(degree);
            inputs = engine.prepareInputsFields(degree, cached);
        } else {
            inputs = engine.parseInputsCsv(inputsCsv, degree);

        }

        ExecutionReport report;
        try {
            report = engine.runWithReportForUser(username,degree, inputs);
        } catch (Exception e) {
            ResponseWriter.write(res, JsonResponseUtils.error("Program execution failed: " + e.getMessage()));
            return;
        }

        if (report == null ) {
            ResponseWriter.write(res, JsonResponseUtils.error("No execution report generated."));
            return;
        }

        if (report.totalCycles() == -1) {
            JsonObject out = JsonResponseUtils.success("OUT_OF_CREDITS");
            out.add("report", new Gson().toJsonTree(report));
            ResponseWriter.write(res, out);
            return;
        }


        // Save run info into user's history
        String architecture = ArchitectureManager.getArchitecture(username, progName);
        int nextRunID = UserHistoryManager.getUserExecHistories(username).size() + 1;
       RunRecord runRecord = new RunRecord(nextRunID, degree, inputs, report.yValue(), report.totalCycles(), report.finalVars());
       String type=SelectedProgramManager.getSelectedType(username);

        UserHistory history = new UserHistory(nextRunID, type, progName, architecture, runRecord);
       UserHistoryManager.addRun(username, history);
        UserInfoManager.addExecution(username);

        // Clear ReRun mode (if it was active)
        if (ReRunStateManager.isReRun(username)) {
            ReRunStateManager.clear(username);
        }

        // Mark update flags
        UpdateFlagsManager.markUpdated("results",username);
        UpdateFlagsManager.markUpdated("history");
        UpdateFlagsManager.markUpdated("users");

        // Send back report
        JsonObject response = JsonResponseUtils.success("Program executed successfully (degree " + degree + ").");
        response.add("report", new Gson().toJsonTree(report));
        response.add("finalVars", new Gson().toJsonTree(report.finalVars()));
        ResponseWriter.write(res, response);
    }
}
