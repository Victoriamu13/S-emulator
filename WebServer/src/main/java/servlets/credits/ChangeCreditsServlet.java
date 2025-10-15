package servlets.credits;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.domain.user.CreditManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/chargeCredits")

public class ChangeCreditsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{

        JsonObject response;
        try{
            String user=req.getParameter("user");
            String action = req.getParameter("action");
            int credits=Integer.parseInt(req.getParameter("credits"));

                switch(action.toUpperCase()){
                    case "DEDUCT"->{
                        boolean success= CreditManager.chargeCredits(user,credits);
                        if(success){
                            response=JsonResponseUtils.success("Credits deducted successfully.");
                            int newCredits = CreditManager.getCredits(user);
                            response.addProperty("credits", newCredits);
                        }else{
                            response=JsonResponseUtils.error("Not enough credits to perform this operation.");
                        }
                    }
                    case "ADD"->{
                        CreditManager.addCredits(user,credits);
                        response=JsonResponseUtils.success("Credits added successfully.");
                        int newCredits = CreditManager.getCredits(user);
                        response.addProperty("credits", newCredits);
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
