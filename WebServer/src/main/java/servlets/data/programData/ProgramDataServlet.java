package servlets.data.programData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.domain.program.SProgram;
import logic.domain.program.functions.FunctionLookup;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.api.EngineFacadeImpl;
import logic.engineFacade.model.InstructionDTO;
import logic.system.data.expansion.DegreeManager;
import logic.system.programs.functions.repository.FuncAsProgAdapter;
import logic.system.programs.functions.repository.FunctionRepository;
import logic.system.programs.repository.ProgramRepository;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.engine.EngineFacadeManager;
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
        String currentUser = ServletUserUtils.getUsernameFromCookies(req);
        if (currentUser == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        // === Get selection (program/function) ===
        String type = SelectedProgramManager.getSelectedType(currentUser);
        String name = SelectedProgramManager.getSelectedProgram(currentUser);

        if (type == null || name == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No program or function selected."));
            return;
        }

        // === Try to get existing engine ===
        EngineFacade engine = EngineFacadeManager.getEngine(currentUser, name);

        // CASE engine not found
        if (engine == null) {
            System.out.println("[ProgramDataServlet] Engine not initialized yet for user=" + currentUser + ", name=" + name);
            ResponseWriter.write(res, JsonResponseUtils.error("Engine not ready yet for this selection."));
            return;
        }

        // ===  Reset cache to ensure fresh data ===
//        try {
//            engine.resetExpansionCache();
//        } catch (Exception e) {
//            System.out.println("[ProgramDataServlet] Warning: failed to reset cache for user=" + currentUser + ": " + e.getMessage());
//        }

        System.out.println("[ProgramDataServlet] Using engine for user=" + currentUser + ", name=" + name);

        // ===  Parse degree parameter  ===
        int degree = DegreeManager.getDegree(currentUser,name);
        String degreeParam = req.getParameter("degree");

        if (degreeParam != null) {
            try {
                degree = Integer.parseInt(degreeParam);
                DegreeManager.setDegree(currentUser,name, degree);
            } catch (NumberFormatException ignored) {
                ResponseWriter.write(res, JsonResponseUtils.error("Invalid degree parameter: " + degreeParam));
                return;
            }
        }

        // ===  Fetch program data ===
        int maxDegree;
        List<InstructionDTO> instructions;

        try {
            maxDegree = engine.getMaxExpansionDegree();
            int usedDegree = Math.max(0, degree);
            instructions = engine.getInstructionRows(usedDegree);

            // ===  Build response JSON ===
            JsonObject response = JsonResponseUtils.success("Fetched program data successfully.");
            response.addProperty("currentDegree", Math.max(0, degree));
            response.addProperty("maxDegree", maxDegree);
            response.add("instructions", new Gson().toJsonTree(instructions));

            UpdateFlagsManager.markUpdated("degree");
            ResponseWriter.write(res, response);

        } catch (Exception e) {
            System.err.println("[ProgramDataServlet] Error building program data for user=" + currentUser + ": " + e.getMessage());
            ResponseWriter.write(res, JsonResponseUtils.error("Failed to load program data: " + e.getMessage()));
        }
    }
}