package servlets.data.programData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.api.EngineFacade;
import logic.system.programs.repository.ProgramRepository;
import logic.system.programs.selectedProg.SelectedProgramManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import logic.system.programs.functions.repository.FunctionRepository;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/programVariables")

public class ProgramVarsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String currentUser = null;
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("username".equals(c.getName())) {
                    currentUser = c.getValue();
                    break;
                }
            }
        }
        if (currentUser == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }
        String progName= SelectedProgramManager.getSelectedProgram(currentUser);
        String type = SelectedProgramManager.getSelectedType(currentUser);

        if(progName==null || type==null){
            ResponseWriter.write(res,JsonResponseUtils.error("No program selected."));
            return;
        }
        EngineFacade engine = null;

        if ("program".equalsIgnoreCase(type)) {
            engine = ProgramRepository.getEngineForProgram(currentUser, progName);
        } else if ("function".equalsIgnoreCase(type)) {
            engine = FunctionRepository.getEngineForFunction(currentUser, progName);
        }

        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Program not found."));
            return;
        }

        int degree = 0;
        try {
            String degreeParam = req.getParameter("degree");
            if (degreeParam != null) degree = Integer.parseInt(degreeParam);
        } catch (NumberFormatException ignored) {}

        List<String> inputs = engine.getInputsUsed(degree);
        List<String> vars = engine.getAllVariablesUsed(degree, null);
        List<String> labels = engine.getAllLabelsUsed(degree, null);

        Set<String> all = new LinkedHashSet<>();
        all.addAll(inputs);
        all.addAll(vars);
        all.addAll(labels);

        JsonObject response = JsonResponseUtils.success("Fetched variable list.");
        response.add("variables", new Gson().toJsonTree(all));
        ResponseWriter.write(res, response);
    }
}

