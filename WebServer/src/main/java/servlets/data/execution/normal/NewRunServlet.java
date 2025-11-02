package servlets.data.execution.normal;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.domain.architecture.ArchitectureGen;
import logic.engineFacade.api.EngineFacade;
import logic.system.data.architecture.ArchitectureManager;
import logic.system.data.execution.runHistory.ReRunStateManager;
import logic.system.data.expansion.DegreeManager;
import logic.system.programs.costs.ProgramAvgCostManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.credits.CreditManager;
import logic.system.user.engine.EngineFacadeManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/newRun")

public class NewRunServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String progName = SelectedProgramManager.getSelectedProgram(username);
        if (progName == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No program selected."));
            return;
        }

        // Check average number of credits costs program before new run
        String selectedArch = ArchitectureManager.getArchitecture(username, progName);
        if (selectedArch == null || selectedArch.isBlank()) {
            ResponseWriter.write(res, JsonResponseUtils.error("No architecture selected."));
            return;
        }

        int archCost = ArchitectureGen.valueOf(selectedArch).getBaseCost();
        double avgCost = ProgramAvgCostManager.getAverageCost(progName);
        int required = (int) Math.ceil(avgCost + archCost);

        int userCredits = CreditManager.getCredits(username);
        if (userCredits < required) {
            JsonObject error = JsonResponseUtils.error("INSUFFICIENT_CREDITS");
            error.addProperty("credits", userCredits);
            error.addProperty("required", required);
            error.addProperty("avgCost", (int) avgCost);
            error.addProperty("archCost", archCost);
            ResponseWriter.write(res, error);
            return;
        }

        EngineFacade engine = EngineFacadeManager.getEngine(username, progName);
        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Engine not found."));
            return;
        }


        if (ReRunStateManager.isReRun(username)) {
            int degree = DegreeManager.getDegree(username,progName);
            List<String> inputs = engine.getCachedInputValues(degree);

            UpdateFlagsManager.markUpdated("inputs",username);
            UpdateFlagsManager.markUpdated("startNewRun",username);

            JsonObject response = JsonResponseUtils.success("ReRun mode — existing inputs preserved.");
            response.add("inputs", new Gson().toJsonTree(inputs));
            ResponseWriter.write(res, response);
            return;
        }

        int degree = DegreeManager.getDegree(username,progName);
        List<String> inputs = engine.loadInputVars(degree);

String arch=ArchitectureManager.getArchitecture(username,progName);

        UpdateFlagsManager.markUpdated("inputs",username);
        UpdateFlagsManager.markUpdated("startNewRun",username);

        JsonObject response=JsonResponseUtils.success("New run initialized successfully.");
        response.add("inputs", new Gson().toJsonTree(inputs));
        System.out.printf(
                "[SERVER CHECK] user=%s | prog=%s | arch=%s | avg=%.2f | archCost=%d | required=%d | credits=%d%n",
                username, progName, arch, avgCost, archCost, required, userCredits
        );
        ResponseWriter.write(res,response);
    }
}
