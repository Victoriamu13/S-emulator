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
import logic.system.data.architecture.ArchitectureManager;
import logic.system.data.expansion.DegreeManager;
import logic.system.data.highlight.DebugHighlightManager;
import logic.system.programs.costs.ProgramAvgCostManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.engine.EngineFacadeManager;
import logic.system.user.engine.EngineService;
import logic.system.user.history.userHstory.UserHistory;
import logic.system.user.history.userHstory.UserHistoryManager;
import logic.system.user.info.UserInfoManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;
import java.util.List;

@WebServlet("/stopDebug")
public class StopDebugServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String program = SelectedProgramManager.getSelectedProgram(username);
        EngineFacade engine = EngineService.ensureEngineForUser(username, program);
        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active engine found."));
            return;
        }

        if (!engine.isDebugActive()) {
            ResponseWriter.write(res, JsonResponseUtils.error("Debug session finished. No more instructions to execute."));
            return;
        }

        ExecutionReport report = engine.stopDebugSession();
        if (report == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active debug session."));
            return;
        }

        int degree = DegreeManager.getDegree(username, program);
        List<String> cachedInputs = engine.getCachedInputValues(degree);
        long[] inputValues = cachedInputs.stream().mapToLong(Long::parseLong).toArray();
        int nextRunId = UserHistoryManager.getOwnHistories(username).size() + 1;
        String progType = SelectedProgramManager.getSelectedType(username);
        String architecture = ArchitectureManager.getArchitecture(username, program);

        RunRecord record = new RunRecord(nextRunId, degree, inputValues, report.yValue(), report.totalCycles(), report.finalVars());

        UserHistory history = new UserHistory(nextRunId, progType, program, architecture, username,record);
        UserHistoryManager.addRun(username, history);
        UserInfoManager.addExecution(username);

        long usedCredits = report.totalCycles();
        ProgramAvgCostManager.updateAverageCost(program, usedCredits);

        //Clear highlight
        DebugHighlightManager.clearHighlight(username);
        UpdateFlagsManager.markUpdated("history");
        UpdateFlagsManager.markUpdated("users");
        UpdateFlagsManager.markUpdated("debugResults",username);
        UpdateFlagsManager.markUpdated("debugInstructionClear",username);

        JsonObject response = JsonResponseUtils.success("Debug stopped.");
        response.add("report", new Gson().toJsonTree(report));
        ResponseWriter.write(res, response);
    }
}
