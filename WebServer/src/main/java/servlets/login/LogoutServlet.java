package servlets.login;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.data.architecture.ArchitectureManager;
import logic.system.data.execution.runHistory.ReRunStateManager;
import logic.system.data.expansion.DegreeManager;
import logic.system.data.highlight.DebugHighlightManager;
import logic.system.data.highlight.HighlightVariableManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.user.credits.CreditManager;
import logic.system.user.engine.EngineFacadeManager;
import logic.system.user.history.selectedUser.SelectedUserManager;
import logic.system.user.history.userHstory.UserHistoryManager;
import logic.system.user.info.UserInfoManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        EngineFacadeManager.clearEnginesForUser(username);
        DegreeManager.clearUser(username);
        ArchitectureManager.clearAllUserArchitectures(username);
        HighlightVariableManager.clearHighlight(username);
        DebugHighlightManager.clearHighlight(username);
        SelectedProgramManager.clear(username);
        SelectedUserManager.clearSelection(username);
        ReRunStateManager.clear(username);
        UserHistoryManager.removeUserHistory(username);
        CreditManager.removeUser(username);
        UserInfoManager.removeUser(username);

        ServletUserUtils.clearUserCookie(res);
        ResponseWriter.write(res, JsonResponseUtils.success("User logged out successfully."));
    }
}

