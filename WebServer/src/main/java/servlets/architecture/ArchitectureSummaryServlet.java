package servlets.architecture;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.ArchitectureSummary;
import logic.system.data.architecture.ArchitectureManager;
import logic.system.data.expansion.DegreeManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.user.engine.EngineFacadeManager;
import logic.system.user.engine.EngineService;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;
import java.util.Map;

@WebServlet("/architectureSummary")
public class ArchitectureSummaryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active session."));
            return;
        }

        String progName = SelectedProgramManager.getSelectedProgram(username);
        if (progName == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active program selection."));
            return;
        }

        EngineFacade engine = EngineService.ensureEngineForUser(username, progName);
        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No engine for user."));
            return;
        }

        int degree = DegreeManager.getDegree(username, progName);

        //Map<String, ArchitectureSummary> summaries = engine.getArchitectureSummary(degree);
        Map<String, ArchitectureSummary> summaries;
        try {
            summaries = engine.getArchitectureSummary(degree);
        } catch (Exception e) {
            ResponseWriter.write(res, JsonResponseUtils.error("Failed to build architecture summary: " + e.getMessage()));
            return;
        }

        JsonObject architectures = new JsonObject();
        summaries.forEach((name, summary) -> {
            JsonObject info = new JsonObject();
            info.addProperty("archName", summary.name());
            info.addProperty("supported", summary.supported());
            info.addProperty("total", summary.total());
            architectures.add(name, info);
        });

        JsonObject response = JsonResponseUtils.success("Architecture Summary");
        response.add("architectures", architectures);
        ResponseWriter.write(res, response);
    }
}
