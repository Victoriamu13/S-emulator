package servlets.data.programData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.InstructionDTO;
import logic.system.data.expansion.DegreeManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.engine.EngineFacadeManager;
import logic.system.user.engine.EngineService;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;
import java.util.List;

@WebServlet("/programData")
public class ProgramDataServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

        // ===  Get current user ===
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        // === Get selection (program/function) ===
        String type = SelectedProgramManager.getSelectedType(username);
        String name = SelectedProgramManager.getSelectedProgram(username);

        if (type == null || name == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No program or function selected."));
            return;
        }

        // === Try to get existing engine ===
        EngineFacade engine = EngineService.ensureEngineForUser(username, name);

        // CASE engine not found
        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Engine not ready yet for this selection."));
            return;
        }

        // ===  Parse degree parameter  ===
        int degree = DegreeManager.getDegree(username,name);
        String degreeParam = req.getParameter("degree");

        if (degreeParam != null) {
            try {
                degree = Integer.parseInt(degreeParam);
                DegreeManager.setDegree(username,name, degree);
            } catch (NumberFormatException ignored) {
                ResponseWriter.write(res, JsonResponseUtils.error("Invalid degree parameter: " + degreeParam));
                return;
            }
        }

        // ===  Fetch program data ===
        int maxDegree;
        List<InstructionDTO> instructions;

            maxDegree = engine.getMaxExpansionDegree();
            int usedDegree = Math.max(0, degree);
            instructions = engine.getInstructionRows(usedDegree);

            // ===  Build response JSON ===
            JsonObject response = JsonResponseUtils.success("Fetched program data successfully.");
            response.addProperty("currentDegree", Math.max(0, degree));
            response.addProperty("maxDegree", maxDegree);
            response.add("instructions", new Gson().toJsonTree(instructions));

            UpdateFlagsManager.markUpdated("degree",username);
            ResponseWriter.write(res, response);
    }
}