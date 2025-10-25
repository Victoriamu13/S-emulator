package servlets.updates;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.ResponseWriter;

import java.io.IOException;

@WebServlet("/historyChainUpdated")

public class HistoryChainUpdatedServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{
        JsonObject response=new JsonObject();
        boolean updated= UpdateFlagsManager.hasUpdated("historyChain");
        response.addProperty("updated",updated);

        if(updated) UpdateFlagsManager.clearFlag("historyChain");
        ResponseWriter.write(res,response);
    }
}
