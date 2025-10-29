package servlets.updates.dataUpdates;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.ResponseWriter;

import java.io.IOException;

@WebServlet("/programVariablesUpdated")
public class ProgramVarsUpdatedServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        boolean updated = UpdateFlagsManager.hasUpdated("programVariables");
        JsonObject obj = new JsonObject();
        obj.addProperty("updated", updated);

        if (updated) UpdateFlagsManager.clearFlag("programVariables");
        ResponseWriter.write(res, obj);
    }
}
