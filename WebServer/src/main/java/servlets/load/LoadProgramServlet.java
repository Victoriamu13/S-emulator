package servlets.load;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import logic.system.api.SystemManager;
import logic.system.api.SystemManagerImpl;
import logic.system.validation.ProgramValidation;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.api.EngineFacadeImpl;
import logic.engineFacade.model.LoadOutcome;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;

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
            String username=null;
            if(req.getCookies()!=null){
                for(Cookie cookie : req.getCookies()){
                    if("username".equals(cookie.getName())){
                        username=cookie.getValue();
                        break;
                    }
                }
            }

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
                EngineFacade engine=new EngineFacadeImpl();
                LoadOutcome outcome=engine.loadProgram(inputStream);

                if(outcome.success()){
                    List<String> validationErrors = ProgramValidation.validateAndRegister(engine);

                    if(!validationErrors.isEmpty()) {
                        response = JsonResponseUtils.error(String.join(", ", validationErrors));
                    }else {
                        systemManager.addProgram(username,engine);
                        response = JsonResponseUtils.success("Program loaded successfully.");
                    }

                }else{
                    response = JsonResponseUtils.error(String.join(", ", outcome.errors()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response = JsonResponseUtils.error("Server error: " + e.getMessage());
        }
        ResponseWriter.write(res, response);
    }
}
