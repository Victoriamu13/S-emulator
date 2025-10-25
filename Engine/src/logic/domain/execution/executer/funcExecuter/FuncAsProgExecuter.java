package logic.domain.execution.executer.funcExecuter;

import logic.domain.program.SProgram;
import logic.system.programs.functions.repository.FuncAsProgAdapter;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.api.EngineFacadeImpl;
import logic.engineFacade.model.ExecutionReport;
import logic.system.programs.functions.repository.FunctionEntry;
import logic.system.programs.functions.repository.FunctionRepository;

public class FuncAsProgExecuter {

    // Runs a function (by internal name) as a standalone temporary program
    public static ExecutionReport runFunctionAsProgram(String functionName){
        if(functionName == null || functionName.isEmpty()){
            return ExecutionReport.empty();
        }

        if(!FunctionRepository.functionExists(functionName)){
            return ExecutionReport.empty();
        }

        FunctionEntry entry= FunctionRepository.getFunctionEntry(functionName);
        FuncAsProgAdapter adapter=new FuncAsProgAdapter(functionName, FunctionRepository.asLookup());

        SProgram tempProgram=adapter.asProgram();
        if(tempProgram==null){
            return ExecutionReport.empty();
        }

        EngineFacade engine=new EngineFacadeImpl();
        engine.loadExistingProgram(tempProgram);

        ExecutionReport report=engine.runWithReport(0);
        return report;

    }
}
