package servlets.user;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.domain.user.CreditManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;

import java.io.IOException;

@WebServlet("/currentUser")

public class CurrentUserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        JsonObject response;
        String username=null;

        if(req.getCookies()!=null){
            for(Cookie cookie :req.getCookies()){
                if("username".equals(cookie.getName())){
                    username= cookie.getValue();
                    break;
                }
            }
        }
        if(username!=null){
            int credits= CreditManager.getCredits(username);
            response = JsonResponseUtils.success("Current user fetched successfully.");
            response.addProperty("username", username);
            response.addProperty("credits", credits);
        } else {
            response = JsonResponseUtils.error("No active user session.");
        }
        ResponseWriter.write(res, response);
    }
}
