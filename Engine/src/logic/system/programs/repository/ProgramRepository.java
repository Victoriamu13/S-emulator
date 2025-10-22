package logic.system.programs.repository;

import logic.domain.program.SProgram;
import logic.domain.program.functions.FunctionLookup;
import logic.engineFacade.api.EngineFacade;
import logic.system.updates.UpdateFlagsManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProgramRepository {

    private static final Map<String, ProgramEntry> programs = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Set<String> allFunctions=ConcurrentHashMap.newKeySet();

    private ProgramRepository(){}

    public static synchronized boolean programExists(String programName) {
        if (programName == null) return false;
        return allFunctions.contains(programName.trim().toUpperCase(Locale.ROOT));
    }

    public static synchronized void addProgram(String user, EngineFacade engine) {
        SProgram program = engine.getProgram();
        String programName = engine.getProgramName();
        String key = (user + ":" + programName).toUpperCase(Locale.ROOT);

        FunctionLookup lookup = program.getFunctionLookup();
        Set<String> funcs = lookup.allFunctionNames();
        funcs.forEach(fn -> allFunctions.add(fn.toUpperCase(Locale.ROOT)));

        programs.put(key, new ProgramEntry(
                programName,
                user,
                program.getInstructions().size(),
                engine.getMaxExpansionDegree(),
                0,
                0.0
        ));
        UpdateFlagsManager.markUpdated("programs");
    }

    public static synchronized Collection<ProgramEntry> allPrograms() {
        return programs.values();
    }


}
















