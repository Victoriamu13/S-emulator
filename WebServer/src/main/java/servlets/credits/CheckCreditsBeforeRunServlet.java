package servlets.credits;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.domain.architecture.ArchitectureGen;
import logic.system.data.architecture.ArchitectureManager;
import logic.system.programs.costs.ProgramAvgCostManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.user.credits.CreditManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;


@WebServlet("/checkCreditsBeforeRun")
public class CheckCreditsBeforeRunServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active session."));
            return;
        }

        String progName = SelectedProgramManager.getSelectedProgram(username);
        if (progName == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No selected program."));
            return;
        }

        // Get selected architecture
        String arch = ArchitectureManager.getArchitecture(username, progName);
        if (arch == null || arch.isBlank()) {
            ResponseWriter.write(res, JsonResponseUtils.error("No architecture selected."));
            return;
        }

        int archCost = ArchitectureGen.valueOf(arch).getBaseCost();
        double avgCost = ProgramAvgCostManager.getAverageCost(progName);
        int required = (int) Math.ceil(avgCost + archCost);
        int userCredits = CreditManager.getCredits(username);

        JsonObject response;
        if (userCredits < required) {
            response = JsonResponseUtils.error("INSUFFICIENT_CREDITS");
        }
        else{
            response = JsonResponseUtils.success("ENOUGH_CREDITS");
        }
        response.addProperty("credits", userCredits);
        response.addProperty("archCost", archCost);
        response.addProperty("avgCost", avgCost);
        response.addProperty("required", required);
        ResponseWriter.write(res, response);
    }
}
