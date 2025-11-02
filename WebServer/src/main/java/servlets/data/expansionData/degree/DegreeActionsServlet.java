package servlets.data.expansionData.degree;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import logic.system.data.expansion.FinalIndexManager;
import logic.system.data.highlight.HighlightVariableManager;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.data.expansion.DegreeManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/degreeAction")

public class DegreeActionsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String progName = SelectedProgramManager.getSelectedProgram(username);
        if (progName == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active program selection."));
            return;
        }

        int newDegree = 0;
        try {
            newDegree = Integer.parseInt(req.getParameter("degree"));

        } catch (Exception ignored) {}

        DegreeManager.setDegree(username,progName, newDegree);
        UpdateFlagsManager.markUpdated("degree",username);
        FinalIndexManager.clear(username);

        HighlightVariableManager.clearHighlight(username);
        UpdateFlagsManager.markUpdated("programVariables",username);
        UpdateFlagsManager.markUpdated("highlight",username);
        UpdateFlagsManager.markUpdated("architectureSummary",username);

        JsonObject response = JsonResponseUtils.success("Degree updated successfully.");
        response.addProperty("degree", newDegree);
        ResponseWriter.write(res, response);
    }
}