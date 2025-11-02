package servlets.data.programData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.api.EngineFacade;
import logic.system.data.expansion.FinalIndexManager;
import logic.system.programs.repository.ProgramRepository;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import logic.system.programs.functions.repository.FunctionRepository;
import servlets.utils.ServletUserUtils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@WebServlet("/programVariables")

public class ProgramVarsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }
        String progName= SelectedProgramManager.getSelectedProgram(username);
        String type = SelectedProgramManager.getSelectedType(username);

        if(progName==null || type==null){
            ResponseWriter.write(res,JsonResponseUtils.error("No program selected."));
            return;
        }
        EngineFacade engine = null;

        if ("program".equalsIgnoreCase(type)) {
            engine = ProgramRepository.getEngineForProgram(username, progName);
        } else if ("function".equalsIgnoreCase(type)) {
            engine = FunctionRepository.getEngineForFunction(username, progName);
        }

        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Program not found."));
            return;
        }

        int degree = 0;
        Integer finalIndex = null;
        try {
            String degreeParam = req.getParameter("degree");
            if (degreeParam != null) degree = Integer.parseInt(degreeParam);

            String indexParam = req.getParameter("finalIndex");
            if (indexParam != null && !indexParam.isBlank()) {
                finalIndex = Integer.parseInt(indexParam);
            } else {
                finalIndex = FinalIndexManager.get(username);
            }
        } catch (NumberFormatException ignored) {}

        Integer prevFinalIndex = FinalIndexManager.get(username);
        if (finalIndex != null) FinalIndexManager.set(username, finalIndex);

        // === Collect Variables ===
        Set<String> all = new LinkedHashSet<>();

        // regular variables from current degree
        all.addAll(engine.getInputsUsed(degree));
        all.addAll(engine.getAllVariablesUsed(degree, finalIndex));
        all.addAll(engine.getAllLabelsUsed(degree, finalIndex));

        JsonObject response = JsonResponseUtils.success("Fetched variable list.");
        response.add("variables", new Gson().toJsonTree(all));

        boolean degreeChanged = UpdateFlagsManager.hasUpdated("degree",username);
        boolean finalIndexChanged =(!Objects.equals(prevFinalIndex, finalIndex));

        if (degreeChanged || finalIndexChanged) {
            UpdateFlagsManager.markUpdated("programVariables",username);
        }
        ResponseWriter.write(res, response);
    }
}

