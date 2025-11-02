package servlets.user.history.selectedUser;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.history.selectedUser.SelectedUserManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/selectedUser")

public class SelectedUserServlet extends HttpServlet {

    //Update history table to show the selected user's history
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException{
        String selected  = req.getParameter("user");  //selected user that client wants to view
        String requester  = ServletUserUtils.getUsernameFromCookies(req);
        JsonObject response;

        if (requester == null || selected == null || selected.isBlank()) {
            response = JsonResponseUtils.error("Missing parameters.");
            ResponseWriter.write(res, response);
            return;
        }

        SelectedUserManager.setSelectedUser(requester, selected);
        UpdateFlagsManager.markUpdated("history");

        response = JsonResponseUtils.success("Selected user: " + selected);
        ResponseWriter.write(res, response);
    }



    //Fetch the currently selected user for the logged-in user
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{
        String currentUser = null; //current logged-in user

        if(req.getCookies()!=null){
            for(Cookie c: req.getCookies()){
                if("username".equals(c.getName())){ //find current logged-in user from cookies
                    currentUser = c.getValue();
                    break;
                }
            }
        }

        if(currentUser==null){ //no logged-in user found
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String selected=SelectedUserManager.getSelectedUser(currentUser); //get selected user for current logged-in user

        JsonObject response=JsonResponseUtils.success("Fetched selected user successfully.");
        response.addProperty("selected",selected);
        ResponseWriter.write(res,response);
    }
}
