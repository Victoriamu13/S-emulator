package servlets.selection;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.domain.program.SProgram;
import logic.domain.program.functions.FunctionLookup;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.api.EngineFacadeImpl;
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
import com.google.gson.JsonObject;
import java.io.IOException;

@WebServlet("/selected")
public class SelectedServlet extends HttpServlet {

    // Update selection
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String type = req.getParameter("type");     // program / function
        String name = req.getParameter("name");     // Name from client


        if (type == null || name == null || type.isBlank() || name.isBlank()) {
            ResponseWriter.write(res, JsonResponseUtils.error("Missing selection data."));
            return;
        }

        // Normalize case
        type = type.toLowerCase().trim();

        // Save new selection for this user
        SelectedProgramManager.setSelectedProgram(username, type, name);
        UpdateFlagsManager.markUpdated("degree",username);


        EngineFacade engine = null;
        if (EngineFacadeManager.hasEngine(username, name)) {
            engine = EngineFacadeManager.getEngine(username, name);
        } else {
            // Create engine –> only if not already existing
            if ("program".equals(type)) { //Case user has an existing engine for program -> take it from repo
                engine = ProgramRepository.getEngineForProgram(username, name);

                if (engine == null) { //Build program based on the original one in repo
                    SProgram program = ProgramRepository.getProgramByName(name);
                    if (program != null) {
                        engine = new EngineFacadeImpl();
                        engine.loadExistingProgram(program);
                        EngineFacadeManager.registerEngine(username, name, engine);
                    } else {
                        ResponseWriter.write(res, JsonResponseUtils.error("Program '" + name + "' not found in repository."));
                        return;
                    }
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
                DegreeManager.setDegree(username, name, 0);
            }
        }

        // Send respond to client
        JsonObject response = JsonResponseUtils.success("Selection processed.");
        response.addProperty("type", type);
        response.addProperty("name", name);
        response.addProperty("ready", engine!=null);
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

        //Send response
        JsonObject response = JsonResponseUtils.success("Fetched current selection successfully.");
        response.addProperty("type", type);
        response.addProperty("name", name);

        ResponseWriter.write(res, response);
    }
}
