package servlets.data.execution.normal;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.data.execution.runHistory.ReRunStateManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/isReRun")
public class IsReRunServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Determine username from cookies
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        // Check if this user currently in ReRun mode
        boolean reRun = ReRunStateManager.isReRun(username);

        JsonObject response = JsonResponseUtils.success("ReRun state checked.");
        response.addProperty("reRun", reRun);
        ResponseWriter.write(res, response);
    }
}