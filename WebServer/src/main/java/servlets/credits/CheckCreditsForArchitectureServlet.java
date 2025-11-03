package servlets.credits;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.domain.architecture.ArchitectureGen;
import logic.system.data.architecture.ArchitectureManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.user.credits.CreditManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/checkCreditsForArchitecture")
public class CheckCreditsForArchitectureServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active session."));
            return;
        }

        String progName = SelectedProgramManager.getSelectedProgram(username);
        if (progName == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No program selected."));
            return;
        }


        String selectedArch = ArchitectureManager.getArchitecture(username, progName);
        if (selectedArch == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No architecture selected."));
            return;
        }


        int cost = ArchitectureGen.valueOf(selectedArch).getBaseCost();
        int credits = CreditManager.getCredits(username);

        if (credits < cost) {
            JsonObject error = JsonResponseUtils.error("Not enough credits for " + selectedArch + " (" + credits + "/" + cost + ").");
            ResponseWriter.write(res, error);
        } else {
            JsonObject success = JsonResponseUtils.success("User has enough credits (" + credits + "/" + cost + ").");
            success.addProperty("state", "SUCCESS");
            success.addProperty("credits", credits);
            ResponseWriter.write(res, success);
        }
    }
}
