package servlets.user.info;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.api.SystemManager;
import logic.system.api.SystemManagerImpl;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;

import java.io.IOException;

@WebServlet("/currentUser")

public class CurrentUserServlet extends HttpServlet {

    private final SystemManager systemManager=new SystemManagerImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{
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
            int credits= systemManager.getCredits(username);
            response = JsonResponseUtils.success("Current user fetched successfully.");
            response.addProperty("username", username);
            response.addProperty("credits", credits);
        } else {
            response = JsonResponseUtils.error("No active user session.");
        }
        ResponseWriter.write(res, response);
    }
}
