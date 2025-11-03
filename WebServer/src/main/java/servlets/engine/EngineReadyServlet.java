package servlets.engine;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.user.engine.EngineFacadeManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/engineReady")
public class EngineReadyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String prog = SelectedProgramManager.getSelectedProgram(username);
        boolean ready = EngineFacadeManager.hasEngine(username, prog);

        JsonObject response = JsonResponseUtils.success("Engine readiness checked.");
        response.addProperty("ready", ready);
        ResponseWriter.write(res, response);
    }
}