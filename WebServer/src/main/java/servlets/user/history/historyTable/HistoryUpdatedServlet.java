package servlets.user.history.historyTable;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.ResponseWriter;

import java.io.IOException;

@WebServlet("/historyUpdated")

public class HistoryUpdatedServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        boolean updated= UpdateFlagsManager.hasUpdated("history");

        JsonObject response=new JsonObject();
        response.addProperty("updated", updated);

        ResponseWriter.write(res,response);
    }
}
