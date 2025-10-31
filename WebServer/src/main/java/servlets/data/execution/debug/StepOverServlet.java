package servlets.data.execution.debug;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.ExecutionReport;
import logic.engineFacade.model.RunRecord;
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
import java.util.List;

@WebServlet("/stepOver")

public class StepOverServlet extends HttpServlet {
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

        if (!engine.isDebugActive()) {
            ResponseWriter.write(res, JsonResponseUtils.error("Debug session already finished. No more instructions to execute."));
            return;
        }

        // Run one step
        ExecutionReport report = engine.stepOver();
        if (report == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active debug session."));
            return;
        }

        // Mark results updated for the client
        UpdateFlagsManager.markUpdated("debugResults");

        //If finish debug session
        if (!engine.isDebugActive()) {
            System.out.println("[StepOver] 🟢 Debug session finished — saving run to history.");

            int degree = DegreeManager.getDegree(username, program);
            List<String> cachedInputs = engine.getCachedInputValues(degree);

            long[] inputValues = cachedInputs.stream().mapToLong(Long::parseLong).toArray();

            int nextRunId = UserHistoryManager.getUserExecHistories(username).size() + 1;
            String progType = SelectedProgramManager.getSelectedType(username);

            RunRecord record = new RunRecord(nextRunId, degree, inputValues, report.yValue(), report.totalCycles(), report.finalVars());
            UserHistory history = new UserHistory(nextRunId, progType, program, null, record);
            UserHistoryManager.addRun(username, history);

            // Add to history
            UpdateFlagsManager.markUpdated("history");
            System.out.println("[StepOver] ✅ Added finished debug run to user history (" + username + ")");
        }
        JsonObject response = JsonResponseUtils.success("Step executed successfully.");
        response.add("report", new Gson().toJsonTree(report));
        ResponseWriter.write(res, response);
    }
}
