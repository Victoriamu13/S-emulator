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
import logic.system.data.execution.runHistory.ReRunStateManager;
import logic.system.data.expansion.DegreeManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.engine.EngineFacadeManager;
import logic.system.user.history.userHstory.UserHistory;
import logic.system.user.history.userHstory.UserHistoryManager;
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
        System.out.println("[Server] /runProgram received inputsCsv = " + inputsCsv);
        System.out.println("[RunProgram] inputsCsv=" + inputsCsv);
        System.out.println("[RunProgram] Cached values: " + engine.getCachedInputValues(degree));
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
            System.out.println("[Server] Parsed inputs = " + Arrays.toString(inputs));
            report = engine.runWithReport(degree, inputs);
        } catch (Exception e) {
            ResponseWriter.write(res, JsonResponseUtils.error("Program execution failed: " + e.getMessage()));
            return;
        }

        if (report == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No execution report generated."));
            return;
        }

        // Save run info into user's history
       int nextRunID = UserHistoryManager.getUserExecHistories(currentUser).size() + 1;
       RunRecord runRecord = new RunRecord(nextRunID, degree, inputs, report.yValue(), report.totalCycles(), report.finalVars());
       String type=SelectedProgramManager.getSelectedType(currentUser);

        UserHistory history = new UserHistory(nextRunID, type, progName, null, runRecord);
       UserHistoryManager.addRun(currentUser, history);

        // Clear ReRun mode (if it was active)
        if (ReRunStateManager.isReRun(currentUser)) {
            ReRunStateManager.clear(currentUser);
            System.out.println("[Server] ReRun completed → cleared ReRun state for " + currentUser);
        }

        // Mark update flags
        UpdateFlagsManager.markUpdated("results");
       // UpdateFlagsManager.markUpdated("inputs");
        UpdateFlagsManager.markUpdated("history");
        System.out.println("[Server] Program executed successfully — flags updated (results, inputs, history, users)");

        // Send back report
        JsonObject response = JsonResponseUtils.success("Program executed successfully (degree " + degree + ").");
        response.add("report", new Gson().toJsonTree(report));
        response.add("finalVars", new Gson().toJsonTree(report.finalVars()));
        ResponseWriter.write(res, response);
    }
}
