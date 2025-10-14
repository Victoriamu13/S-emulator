package servlets.header;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/chargeCredits")

public class ChangeCreditsServlet extends HttpServlet {
    private final Gson gson=new Gson();
    private static final ConcurrentHashMap<String,Integer> userCredits=new ConcurrentHashMap<>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{

        JsonObject response;
        try{
            String user=req.getParameter("user");
            int credits=Integer.parseInt(req.getParameter("credits"));
            userCredits.put(user,userCredits.getOrDefault(user,0)+credits);
            int newCredits=userCredits.get(user);

            response = JsonResponseUtils.success("Credits updated successfully.");
            response.addProperty("credits", newCredits);
        }catch(Exception e){
            response = JsonResponseUtils.error("Failed to update credits.");
        }
        ResponseWriter.write(res, response);
    }
}
