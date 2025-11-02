package servlets.user.history.selectedUser;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.history.selectedUser.SelectedUserManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;


@WebServlet("/unselectUser")
public class UnselectUserServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        JsonObject response;

        if (username == null) {
            response = JsonResponseUtils.error("No active user found.");
            ResponseWriter.write(res, response);
            return;
        }

        SelectedUserManager.clearSelection(username);
        UpdateFlagsManager.markUpdated("history");

        response = JsonResponseUtils.success("User unselected successfully.");
        ResponseWriter.write(res, response);
    }
}
