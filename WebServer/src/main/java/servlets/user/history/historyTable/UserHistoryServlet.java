package servlets.user.history.historyTable;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.user.history.selectedUser.SelectedUserManager;
import logic.system.user.history.userHstory.UserHistory;
import logic.system.user.history.userHstory.UserHistoryManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;
import java.util.List;

@WebServlet("/userHistory")

public class UserHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if(username==null){ //no logged-in user found
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String selectedUser= SelectedUserManager.getSelectedUser(username); //get selected user for current logged-in user;
        String targetUser = (selectedUser != null) ? selectedUser : username;

        List<UserHistory> userHistory= UserHistoryManager.getUserExecHistories(username); //get selected user's history
        ResponseWriter.write(res, userHistory);

    }
}
