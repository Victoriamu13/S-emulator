package servlets.updates.dataUpdates;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.ResponseWriter;

import java.io.IOException;

@WebServlet("/degreeUpdated")

public class DegreeUpdatedServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{
        JsonObject response=new JsonObject();
        boolean updated= UpdateFlagsManager.hasUpdated("degree");

        response.addProperty("updated",updated);

        if(updated){
            UpdateFlagsManager.clearFlag("degree");

        }
        ResponseWriter.write(res, response);
    }
}
