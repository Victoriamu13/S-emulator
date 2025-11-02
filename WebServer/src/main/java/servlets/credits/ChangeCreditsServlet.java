package servlets.credits;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.api.SystemManager;
import logic.system.api.SystemManagerImpl;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/chargeCredits")

public class ChangeCreditsServlet extends HttpServlet {

    private final SystemManager systemManager=new SystemManagerImpl();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException{
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active session."));
            return;
        }

        JsonObject response;
        try{
            String action = req.getParameter("action");
            int credits=Integer.parseInt(req.getParameter("credits"));

                switch(action.toUpperCase()){
                    case "DEDUCT"->{
                        boolean success= systemManager.chargeCredits(username,credits);
                        if(success){
                            response=JsonResponseUtils.success("Credits deducted successfully.");
                            int newCredits = systemManager.getCredits(username);
                            response.addProperty("credits", newCredits);
                            UpdateFlagsManager.markUpdated("users");
                        }else{
                            response=JsonResponseUtils.error("Not enough credits to perform this operation.");
                        }
                    }
                    case "ADD"->{
                        systemManager.addCredits(username,credits);
                        response=JsonResponseUtils.success("Credits added successfully.");
                        int newCredits = systemManager.getCredits(username);
                        response.addProperty("credits", newCredits);
                        UpdateFlagsManager.markUpdated("users");
                    }
                    default -> response = JsonResponseUtils.error("Unknown action type: must be ADD or DEDUCT.");
                }
        } catch (NumberFormatException e) {
            response = JsonResponseUtils.error("Invalid credit amount (must be a number).");
        } catch (Exception e) {
            response = JsonResponseUtils.error("Server error while updating credits: " + e.getMessage());
        }
        ResponseWriter.write(res, response);
    }
}
