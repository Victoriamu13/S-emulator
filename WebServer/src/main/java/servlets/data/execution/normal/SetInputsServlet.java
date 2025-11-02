package servlets.data.execution.normal;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.api.EngineFacade;
import logic.system.data.expansion.DegreeManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.user.engine.EngineFacadeManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/setInputs")

public class SetInputsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
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

        EngineFacade engine = EngineFacadeManager.getEngine(username, progName);
        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active engine for program."));
            return;
        }

        String inputsCsv = req.getParameter("inputs");
        if (inputsCsv == null || inputsCsv.isEmpty()) {
            inputsCsv = "0";
        }

        int degree = DegreeManager.getDegree(username,progName);

        try {
            long[] parsedInputs = engine.parseInputsCsv(inputsCsv, degree);
            engine.prepareInputsFields(degree, java.util.Arrays.stream(parsedInputs)
                    .mapToObj(String::valueOf)
                    .toList());

            JsonObject response = JsonResponseUtils.success("Inputs saved successfully.");
            response.addProperty("inputs", inputsCsv);
            ResponseWriter.write(res, response);
        } catch (Exception e) {
            ResponseWriter.write(res, JsonResponseUtils.error("Failed to parse inputs: " + e.getMessage()));
        }
    }
}
