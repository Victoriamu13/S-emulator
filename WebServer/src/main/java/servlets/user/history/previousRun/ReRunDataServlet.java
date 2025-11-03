package servlets.user.history.previousRun;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.RunRecord;
import logic.system.data.execution.runHistory.ReRunStateManager;
import logic.system.data.expansion.DegreeManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.user.engine.EngineFacadeManager;
import logic.system.user.history.userHstory.UserHistoryManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/reRunData")
public class ReRunDataServlet extends HttpServlet {  //Activates Re-Run mode for user and loads inputs for client.
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String ownerUser = null;
        String runID = null;
        String progName = null;
        String progType = null;

        // Extract ReRun cookies (set by HistoryTableController)
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                switch (c.getName()) {
                    case "reRunOwner" -> ownerUser = c.getValue();
                    case "reRunID" -> runID = c.getValue();
                    case "reRunProg" -> progName = c.getValue();
                    case "reRunType" -> progType = c.getValue();
                }
            }
        }

        // === Validate that all required cookies exist ===
        if (ownerUser  == null || runID == null || progName == null || progType==null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Missing ReRun cookies."));
            return;
        }

        int runIdInt;
        try {
            runIdInt = Integer.parseInt(runID);
        } catch (NumberFormatException e) {
            ResponseWriter.write(res, JsonResponseUtils.error("Invalid run ID cookie."));
            return;
        }

        // Get record from user's history
        RunRecord record = UserHistoryManager.getRunRecord(ownerUser, runIdInt);
        if (record == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No record found for re-run."));
            return;
        }

        EngineFacade engine = EngineFacadeManager.getEngine(ownerUser, progName);
        if (engine != null) {
            List<String> inputStrings = Arrays.stream(record.inputs())
                    .mapToObj(String::valueOf)
                    .collect(Collectors.toList());

            engine.prepareInputsFields(record.degree(), inputStrings);
        }

        // Build response
        JsonObject response = JsonResponseUtils.success("ReRun data fetched.");
        response.addProperty("degree", record.degree());
        response.add("inputs", new Gson().toJsonTree(record.inputs()));
        ResponseWriter.write(res, response);

    }
}
