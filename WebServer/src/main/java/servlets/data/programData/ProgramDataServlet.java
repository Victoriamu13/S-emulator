package servlets.data.programData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
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
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{

        // find logged-in user from cookies
        String currentUser = ServletUserUtils.getUsernameFromCookies(req);

        if (currentUser == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String type= SelectedProgramManager.getSelectedType(currentUser);
        String name=SelectedProgramManager.getSelectedProgram(currentUser);

        if(type==null || name==null){
            ResponseWriter.write(res, JsonResponseUtils.error("No program or function selected."));
            return;
        }

        EngineFacade engine = EngineFacadeManager.getEngine(currentUser, name);

        if (engine == null) {
            if ("program".equalsIgnoreCase(type)) {
                engine = ProgramRepository.getEngineForProgram(currentUser, name);
            }
            else if ("function".equalsIgnoreCase(type)) {
                String internalName = FunctionRepository.getInternalName(name);

                if (FunctionRepository.functionExists(internalName)) {
                    FunctionLookup lookup = FunctionRepository.asLookup();
                    SProgram program = new FuncAsProgAdapter(internalName, lookup).asProgram();
                    engine = new EngineFacadeImpl();
                    engine.loadExistingProgram(program);
                }
            }
            if (engine == null) {
                ResponseWriter.write(res, JsonResponseUtils.error("Program/Function not found."));
                return;
            }
            EngineFacadeManager.registerEngine(currentUser, name, engine);
        }

        int degree=DegreeManager.getDegree(currentUser);
        String degreeParam=req.getParameter("degree");

        if(degreeParam!=null){
            try{
                degree=Integer.parseInt(degreeParam);
                DegreeManager.setDegree(currentUser,degree);

            }catch (NumberFormatException ignored) {

                ResponseWriter.write(res, JsonResponseUtils.error("Invalid degree parameter: " + degreeParam));
                return;
            }
        }
        int maxDegree=engine.getMaxExpansionDegree();
        int usedDegree=Math.max(0,degree);
        List<InstructionDTO> instructions=engine.getInstructionRows(usedDegree);

        JsonObject response=JsonResponseUtils.success("Fetched program data successfully.");
        response.addProperty("currentDegree",usedDegree);
        response.addProperty("maxDegree",maxDegree);
        response.add("instructions",new Gson().toJsonTree(instructions));

        UpdateFlagsManager.markUpdated("degree");
        ResponseWriter.write(res,response);
    }
}
