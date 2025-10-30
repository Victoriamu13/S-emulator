package servlets.data.execution.normal;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.ResponseWriter;
import java.io.IOException;


@WebServlet("/startNewRun")
public class StartNewRunServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        JsonObject response = new JsonObject();


        boolean updated = UpdateFlagsManager.hasUpdated("startNewRun");
        response.addProperty("updated", updated);


        if (updated) {
            UpdateFlagsManager.clearFlag("startNewRun");
            System.out.println("[Server] startNewRun flag detected and cleared.");
        }

        ResponseWriter.write(res, response);
    }
}