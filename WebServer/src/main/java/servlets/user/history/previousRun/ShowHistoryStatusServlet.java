package servlets.user.history.previousRun;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.model.RunRecord;
import logic.system.user.history.userHstory.UserHistory;
import logic.system.user.history.userHstory.UserHistoryManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;
import java.util.List;

@WebServlet("/showHistoryStatus")
public class ShowHistoryStatusServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String runIdStr = req.getParameter("runID");
        if (runIdStr == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Missing run ID."));
            return;
        }

        int runID = Integer.parseInt(runIdStr);

        List<UserHistory> historyList = UserHistoryManager.getUserExecHistories(username);
        UserHistory selected = historyList.stream()
                .filter(h -> h.runID() == runID)
                .findFirst()
                .orElse(null);

        if (selected == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Run not found."));
            return;
        }

        RunRecord record = selected.runRecord();
        if (record == null || record.finalVars() == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No variable data found."));
            return;
        }

        JsonObject response = JsonResponseUtils.success("History fetched.");
        response.add("finalVars", new Gson().toJsonTree(record.finalVars()));
        ResponseWriter.write(res, response);
    }
}
