package servlets.credits;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.domain.architecture.ArchitectureGen;
import logic.system.user.credits.CreditManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/checkCreditsForArchitecture")
public class CheckCreditsForArchitectureServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String user = ServletUserUtils.getUsernameFromCookies(req);
        String arch = req.getParameter("architecture");

        if (user == null || arch == null) {
            JsonObject error = JsonResponseUtils.error("Missing parameters.");
            ResponseWriter.write(res, error);
            return;
        }

        int cost = ArchitectureGen.valueOf(arch).getBaseCost();
        int credits = CreditManager.getCredits(user);

        if (credits < cost) {
            JsonObject error = JsonResponseUtils.error("Not enough credits for " + arch + " (" + credits + "/" + cost + ").");
            ResponseWriter.write(res, error);
        } else {
            JsonObject success = JsonResponseUtils.success("User has enough credits (" + credits + "/" + cost + ").");
            success.addProperty("state", "SUCCESS");
            success.addProperty("credits", credits);
            ResponseWriter.write(res, success);
        }
    }
}
