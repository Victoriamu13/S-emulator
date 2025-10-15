package logic.domain.program.repository;

import logic.domain.program.SProgram;
import logic.domain.program.functions.FunctionLookup;
import logic.engineFacade.api.EngineFacade;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ProgramRepository {

    private static final Map<String,ProgramEntry> programs=new ConcurrentHashMap<>();
    private static final Set<String> allFunctions=ConcurrentHashMap.newKeySet();

    private ProgramRepository(){}

    public static synchronized boolean programExists(String programName) {
        if (programName == null) return false;
        return allFunctions.contains(programName.trim().toUpperCase(Locale.ROOT));
    }

    public static synchronized void addProgram(String user, EngineFacade engine) {
        SProgram program = engine.getProgram();
        String programName = engine.getProgramName();
        FunctionLookup lookup = program.getFunctionLookup();
        Set<String> funcs = lookup.allFunctionNames();
        funcs.forEach(fn -> allFunctions.add(fn.toUpperCase(Locale.ROOT)));

        programs.put(programName, new ProgramEntry(
                programName,
                user,
                program.getInstructions().size(),
                engine.getMaxExpansionDegree(),
                0,
                0.0
        ));
    }

    public static synchronized Collection<ProgramEntry> allPrograms() {
        return programs.values();
    }

}
















