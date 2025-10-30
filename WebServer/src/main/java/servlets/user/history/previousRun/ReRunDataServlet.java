package servlets.user.history.previousRun;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.model.RunRecord;
import logic.system.data.execution.runHistory.ReRunHistoryManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/reRunData")
public class ReRunDataServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String currentUser = ServletUserUtils.getUsernameFromCookies(req);
        if (currentUser == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        RunRecord record = ReRunHistoryManager.getRecord(currentUser);
        if (record == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No re-run data found."));
            return;
        }

        JsonObject response = JsonResponseUtils.success("Re-run data fetched.");
        response.addProperty("degree", record.degree());
        response.add("inputs", new Gson().toJsonTree(record.inputs()));

        ResponseWriter.write(res, response);
    }
}
