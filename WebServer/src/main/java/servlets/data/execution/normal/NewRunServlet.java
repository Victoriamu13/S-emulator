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
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.engine.EngineFacadeManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;
import java.util.List;

@WebServlet("/newRun")

public class NewRunServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String currentUser = ServletUserUtils.getUsernameFromCookies(req);
        if (currentUser == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String progName = SelectedProgramManager.getSelectedProgram(currentUser);
        if (progName == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No program selected."));
            return;
        }

        EngineFacade engine = EngineFacadeManager.getEngine(currentUser, progName);
        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Engine not found."));
            return;
        }

        engine.resetExpansionCache();
        int degree = DegreeManager.getDegree(currentUser);
        List<String> inputs = engine.loadInputVars(degree);
        System.out.println("[NewRun] prog=" + progName + ", degree=" + degree + ", inputs=" + inputs.size());

        UpdateFlagsManager.markUpdated("inputs");
        UpdateFlagsManager.markUpdated("startNewRun");
        System.out.println("[Server] NewRun initialized — inputs and triggerNewRun updated flag set");

        JsonObject response=JsonResponseUtils.success("New run initialized successfully.");
        response.add("inputs", new Gson().toJsonTree(inputs));
        ResponseWriter.write(res,response);
    }
}
