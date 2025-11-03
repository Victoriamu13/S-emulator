package servlets.architecture.charge;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.domain.architecture.ArchitectureGen;
import logic.engineFacade.api.EngineFacade;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.credits.CreditManager;
import logic.system.user.engine.EngineService;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/architectureCharge")
public class ArchitectureChargeServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String user = ServletUserUtils.getUsernameFromCookies(req);
        if (user == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active session."));
            return;
        }

        String progName = SelectedProgramManager.getSelectedProgram(user);
        if (progName == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No program selected."));
            return;
        }

        String archName = req.getParameter("architecture");
        if (archName == null || archName.isBlank()) {
            ResponseWriter.write(res, JsonResponseUtils.error("No architecture selected."));
            return;
        }

        EngineFacade engine = EngineService.ensureEngineForUser(user, progName);
        boolean ready = (engine != null && engine.getProgram() != null);


        ArchitectureGen arch = ArchitectureGen.valueOf(archName);
        int cost = arch.getBaseCost();

        boolean success = CreditManager.chargeCredits(user, cost);
        if (!success) {
            ResponseWriter.write(res, JsonResponseUtils.error("Not enough credits. Required: " + cost));
            return;
        }

        UpdateFlagsManager.markUpdated("users");
        System.out.println("[ARCH CHARGE] ✅ Payment successful — marking ready=true");

        JsonObject resp = JsonResponseUtils.success("Architecture charged successfully.");
        resp.addProperty("credits", CreditManager.getCredits(user));
        resp.addProperty("cost", cost);
        resp.addProperty("ready", ready);
        ResponseWriter.write(res, resp);
    }
}
