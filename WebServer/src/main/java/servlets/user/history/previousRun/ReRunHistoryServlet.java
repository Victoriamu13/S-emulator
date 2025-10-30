package servlets.user.history.previousRun;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.RunRecord;
import logic.system.data.execution.runHistory.ReRunHistoryManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.engine.EngineFacadeManager;
import logic.system.user.history.userHstory.UserHistory;
import logic.system.user.history.userHstory.UserHistoryManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;
import logic.system.data.expansion.DegreeManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebServlet("/reRunHistory")
public class ReRunHistoryServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String currentUser = ServletUserUtils.getUsernameFromCookies(req);
        if (currentUser == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String runIdStr = req.getParameter("runID");
        if (runIdStr == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Missing run ID."));
            return;
        }

        int runID = Integer.parseInt(runIdStr);
        List<UserHistory> histories = UserHistoryManager.getUserExecHistories(currentUser);
        UserHistory selected = histories.stream()
                .filter(h -> h.runID() == runID)
                .findFirst()
                .orElse(null);

        if (selected == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Run not found."));
            return;
        }

        RunRecord record = selected.runRecord();
        if (record == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No run data found."));
            return;
        }

        ReRunHistoryManager.setRecord(currentUser, record);
        DegreeManager.setDegree(currentUser, record.degree());

        String progName = selected.name();
        String type=SelectedProgramManager.getSelectedType(currentUser);
        SelectedProgramManager.setSelectedProgram(currentUser,type, progName);


        EngineFacade engine = EngineFacadeManager.getEngine(currentUser, progName);
        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Engine not found for program " + progName));
            return;
        }


        List<String> inputStrings = Arrays.stream(record.inputs())
                .mapToObj(String::valueOf)
                .toList();


        engine.prepareInputsFields(record.degree(), inputStrings);

        UpdateFlagsManager.markUpdated("initExecution");

        JsonObject response = JsonResponseUtils.success("Program ready for re-run.");
        response.addProperty("program", progName);
        response.addProperty("degree", record.degree());
        response.add("inputs", new Gson().toJsonTree(record.inputs()));
        ResponseWriter.write(res, response);
    }

}
