package servlets.load;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import logic.system.api.SystemManager;
import logic.system.api.SystemManagerImpl;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.engine.EngineFacadeManager;
import logic.system.validation.ProgramValidation;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.api.EngineFacadeImpl;
import logic.engineFacade.model.LoadOutcome;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@WebServlet("/loadProgram")
@MultipartConfig

public class LoadProgramServlet extends HttpServlet {
 private final SystemManager systemManager=new SystemManagerImpl();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{

        JsonObject response;

        try {
            // === 1) Get user from session ===
            String username = ServletUserUtils.getUsernameFromCookies(req);

            if(username==null || username.isBlank()){
                response = JsonResponseUtils.error("No active user found. Please log in first.");
                ResponseWriter.write(res, response);
                return;
            }


            // === 2) Get file ===
            Part filePart = req.getPart("file");
            if(filePart == null || filePart.getSize()==0){
                throw new IllegalArgumentException("Missing or empty file upload");
            }

            try(InputStream inputStream = filePart.getInputStream()){

                // === 3) Create engine and load program ===
                EngineFacade engine=new EngineFacadeImpl();
                LoadOutcome outcome=engine.loadProgram(inputStream);

                if (!outcome.success()) {
                    response = JsonResponseUtils.error(String.join(", ", outcome.errors()));
                    ResponseWriter.write(res, response);
                    return;
                }

                // === 4) Validate & register program in system ===
                List<String> validationErrors = ProgramValidation.validateAndRegister(engine);
                if (!validationErrors.isEmpty()) {
                    response = JsonResponseUtils.error(String.join(", ", validationErrors));
                    ResponseWriter.write(res, response);
                    return;
                }

                // === 5) Add to system repository ===
                systemManager.addProgram(username, engine);

                // === 6) Register engine globally ===
                String progName = engine.getProgramName();
                EngineFacadeManager.registerEngine(username, progName, engine);

                // === 7) Success response ===
                response = JsonResponseUtils.success(
                        "Program '" + progName + "' loaded and engine initialized successfully.");

                UpdateFlagsManager.markUpdated("programs");
                UpdateFlagsManager.markUpdated("functions");
                UpdateFlagsManager.markUpdated("users");
            }
        } catch (Exception e) {
            response = JsonResponseUtils.error("Server error: " + e.getMessage());
        }
        ResponseWriter.write(res, response);
    }
}
