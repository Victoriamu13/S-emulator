package servlets.header;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import logic.domain.program.validation.ProgramValidation;
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
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{

        JsonObject response;

        try {
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
                        response = JsonResponseUtils.success("Program loaded successfully.");
                    }

                }else{
                    response = JsonResponseUtils.error(String.join(", ", outcome.errors()));
                }
            }
        } catch (Exception e) {
            response = JsonResponseUtils.error("Server error: " + e.getMessage());
        }
        ResponseWriter.write(res, response);
    }
}
