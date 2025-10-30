package servlets.selection;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.domain.program.SProgram;
import logic.domain.program.functions.FunctionLookup;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.api.EngineFacadeImpl;
import logic.system.programs.functions.repository.FuncAsProgAdapter;
import logic.system.programs.functions.repository.FunctionRepository;
import logic.system.programs.repository.ProgramRepository;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.engine.EngineFacadeManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;
import com.google.gson.JsonObject;
import java.io.IOException;

@WebServlet("/selected")
public class SelectedServlet extends HttpServlet {

    // Update selection
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String currentUser = ServletUserUtils.getUsernameFromCookies(req);
        String type = req.getParameter("type");     // program / function
        String name = req.getParameter("name");     // Name from client

        if (currentUser == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        if (type == null || name == null || type.isBlank() || name.isBlank()) {
            ResponseWriter.write(res, JsonResponseUtils.error("Missing selection data."));
            return;
        }

        // Normalize case
        type = type.toLowerCase().trim();
        boolean ready = false;

      // Remember previous selection before overwriting it
        String previousProgram = null;
        if (SelectedProgramManager.hasSelection(currentUser)) {
            previousProgram = SelectedProgramManager.getSelectedProgram(currentUser);
        }
        // Save new selection for this user
        SelectedProgramManager.setSelectedProgram(currentUser, type, name);
        UpdateFlagsManager.markUpdated("degree");

        System.out.println("[SelectedServlet] user=" + currentUser + " selected " + type + "=" + name);

        // Remove old engine if selection changed
        if (previousProgram != null && !previousProgram.equals(name)) {
            EngineFacadeManager.removeEngine(currentUser, previousProgram);
        }

        // Create engine –> only if not already existing
        if (!EngineFacadeManager.hasEngine(currentUser, name)) {
            EngineFacade engine = null;

            if ("program".equals(type)) {
                engine = ProgramRepository.getEngineForProgram(currentUser, name);
                if (engine == null) {
                    engine = new EngineFacadeImpl();
                }
            } else if ("function".equals(type)) {
                String internalName = FunctionRepository.getInternalName(name);
                if (FunctionRepository.functionExists(internalName)) {
                    FunctionLookup lookup = FunctionRepository.asLookup();
                    SProgram program = new FuncAsProgAdapter(internalName, lookup).asProgram();
                    engine = new EngineFacadeImpl();
                    engine.loadExistingProgram(program);
                }
            }

          // Register the new engine
            if (engine != null) {
                ready = true;
                EngineFacadeManager.registerEngine(currentUser, name, engine);
                System.out.println("[SelectedServlet] EngineFacade created for " + type + "=" + name);
            } else {
                System.out.println("[SelectedServlet] EngineFacade creation skipped (invalid selection).");
            }
        } else {
            ready = true;
            System.out.println("[SelectedServlet] Engine already exists for " + type + "=" + name);
        }

        // Send respond to client
        JsonObject response = JsonResponseUtils.success("Selection processed.");
        response.addProperty("type", type);
        response.addProperty("name", name);
        response.addProperty("ready", ready);
        ResponseWriter.write(res, response);
    }

    // Fetch current selection
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String currentUser = ServletUserUtils.getUsernameFromCookies(req);
        if (currentUser == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String type = SelectedProgramManager.getSelectedType(currentUser);
        String name = SelectedProgramManager.getSelectedProgram(currentUser);

        if (type == null || name == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No selection found for user."));
            return;
        }
        System.out.println("[SelectedServlet] user=" + currentUser +
                ", type=" + type + ", name=" + name +
                " -> stored OK");
        //Send response
        JsonObject response = JsonResponseUtils.success("Fetched current selection successfully.");
        response.addProperty("type", type);
        response.addProperty("name", name);

        ResponseWriter.write(res, response);
    }
}
