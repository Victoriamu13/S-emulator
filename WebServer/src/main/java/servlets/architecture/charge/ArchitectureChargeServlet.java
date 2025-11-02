package servlets.architecture.charge;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.domain.architecture.ArchitectureGen;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.credits.CreditManager;
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

        String archName = req.getParameter("architecture");
        if (archName == null || archName.isBlank()) {
            ResponseWriter.write(res, JsonResponseUtils.error("No architecture selected."));
            return;
        }

        ArchitectureGen arch = ArchitectureGen.valueOf(archName);
        int cost = arch.getBaseCost();

        boolean success = CreditManager.chargeCredits(user, cost);
        if (!success) {
            ResponseWriter.write(res, JsonResponseUtils.error("Not enough credits. Required: " + cost));
            return;
        }

        UpdateFlagsManager.markUpdated("users");

        JsonObject resp = JsonResponseUtils.success("Architecture charged successfully.");
        resp.addProperty("credits", CreditManager.getCredits(user));
        resp.addProperty("cost", cost);
        ResponseWriter.write(res, resp);
    }
}
