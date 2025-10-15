package logic.domain.program.validation;

import logic.domain.instructions.SInstruction;
import logic.domain.program.functions.FunctionLookup;
import logic.domain.program.functions.GlobalFunctionRepository;
import logic.domain.program.repository.ProgramRepository;
import logic.engineFacade.api.EngineFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProgramValidation {

    public static List<String> validateAndRegister(EngineFacade engine){
        List<String> errors=new ArrayList<>();
        String progName=engine.getProgramName();

        // === 1. Program name ===
        if(engine.getProgram()==null ||progName.isBlank()) {
            errors.add("No program loaded");
        }else if(ProgramRepository.programExists(progName)){
            errors.add("Program '" + progName + "' already exists in the system.");
        }

        // === 2. Validate functions ===
        FunctionLookup lookup=engine.getProgram().getFunctionLookup();
        Set<String> localFunctions=lookup.allFunctionNames();

        for(String func : localFunctions){
           List<SInstruction> localBody= lookup.bodyOf(func);

           // Case 1: Function declared but not implemented locally or globally
           if(localBody.isEmpty() || localBody==null){
               if(!GlobalFunctionRepository.functionExists(func)){
                   errors.add("Function '" + func + "' declared without implementation and not found globally.");
               }
               continue;
           }

           //Case 2: Function implemented locally, check for idencity with global
            if(GlobalFunctionRepository.functionExists(func)){
              List<SInstruction> globalBody=GlobalFunctionRepository.getFunctionBody(func);
              if(!GlobalFunctionRepository.compareBodies(localBody,globalBody)){
                  errors.add("Function '" + func + "' has a different implementation than the one in the system.");
              }
            }
        }

        return errors;
    }
}
