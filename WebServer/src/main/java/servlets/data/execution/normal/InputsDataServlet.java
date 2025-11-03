package servlets.data.execution.normal;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.api.EngineFacade;
import logic.system.data.expansion.DegreeManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.user.engine.EngineFacadeManager;
import logic.system.user.engine.EngineService;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/inputs")
public class InputsDataServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String progName = SelectedProgramManager.getSelectedProgram(username);
        if (progName == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No selected program."));
            return;
        }

        EngineFacade engine = EngineService.ensureEngineForUser(username, progName);
        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No engine."));
            return;
        }

        int degree = DegreeManager.getDegree(username,progName);
        var inputs = engine.getInputsUsed(degree);
        var values = engine.getCachedInputValues(degree);

        JsonObject out = JsonResponseUtils.success("Inputs fetched.");
        out.add("inputs", new Gson().toJsonTree(inputs));
        out.add("values", new Gson().toJsonTree(values));
        ResponseWriter.write(res, out);
    }
}
