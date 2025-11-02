package servlets.reset;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.data.architecture.ArchitectureManager;
import logic.system.data.expansion.DegreeManager;
import logic.system.data.highlight.DebugHighlightManager;
import logic.system.data.highlight.HighlightVariableManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/resetExecutionState")
public class ResetExecutionStateServlet extends HttpServlet {
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

        DegreeManager.resetDegree(username,progName);
        HighlightVariableManager.clearHighlight(username);
        DebugHighlightManager.clearHighlight(username);
        ArchitectureManager.clearUserArchitecture(username,progName);


        UpdateFlagsManager.markUpdated("initExecution",username);
        UpdateFlagsManager.markUpdated("degreeUpdated",username);
        UpdateFlagsManager.markUpdated("highlightInstructionsClear",username);
        UpdateFlagsManager.markUpdated("highlightHistoryClear",username);
        UpdateFlagsManager.markUpdated("HighlightComboClear",username);
        UpdateFlagsManager.markUpdated("debugInstructionClear",username);
        UpdateFlagsManager.markUpdated("architectureComboClear",username);
        UpdateFlagsManager.markUpdated("architectureLabelClear",username);

        ResponseWriter.write(res, JsonResponseUtils.success("Execution state reset successfully."));
    }
}
