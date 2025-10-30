package servlets.reset;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.data.expansion.DegreeManager;
import logic.system.data.highlight.HighlightManager;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.history.userHstory.UserHistoryManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/resetExecutionState")
public class ResetExecutionStateServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String user = ServletUserUtils.getUsernameFromCookies(req);
        if (user == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        DegreeManager.resetDegree(user);
        UserHistoryManager.clearHistory(user);
        HighlightManager.clearHighlight(user);


        UpdateFlagsManager.markUpdated("initExecution");
        UpdateFlagsManager.markUpdated("degreeUpdated");
        UpdateFlagsManager.markUpdated("highlightInstructionsClear");
        UpdateFlagsManager.markUpdated("highlightHistoryClear");
        UpdateFlagsManager.markUpdated("HighlightComboClear");

        System.out.println("[ResetExecutionStateServlet] Fully cleared execution state for user=" + user);

        ResponseWriter.write(res, JsonResponseUtils.success("Execution state reset successfully."));
    }
}
