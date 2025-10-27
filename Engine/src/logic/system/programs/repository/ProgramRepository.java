package logic.system.programs.repository;

import logic.domain.program.SProgram;
import logic.domain.program.functions.FunctionLookup;
import logic.engineFacade.api.EngineFacade;
import logic.system.updates.UpdateFlagsManager;
import logic.system.user.engine.EngineFacadeManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProgramRepository {

    private static final Map<String, ProgramEntry> programs = Collections.synchronizedMap(new LinkedHashMap<>());

    private ProgramRepository(){}

    public static synchronized boolean programExists(String programName) {
        if (programName == null) return false;
        return programs.values()
                .stream()
                .anyMatch(p -> p.getProgName().equalsIgnoreCase(programName));
    }

    public static synchronized void addProgram(String user, EngineFacade engine) {
        SProgram program = engine.getProgram();
        String programName = engine.getProgramName();
        String key = (user + ":" + programName).toUpperCase(Locale.ROOT);

        programs.put(key, new ProgramEntry(
                programName,
                user,
                program.getInstructions().size(),
                engine.getMaxExpansionDegree(),
                0,
                0.0
        ));
        EngineFacadeManager.registerEngine(user, programName, engine);
        UpdateFlagsManager.markUpdated("programs");
    }

    public static synchronized EngineFacade getEngineForProgram(String user, String progName) {
        return EngineFacadeManager.getEngine(user, progName);
    }

    public static synchronized Collection<ProgramEntry> allPrograms() {
        return programs.values();
    }


}
















