package logic.system.user.engine;

import logic.domain.program.SProgram;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.api.EngineFacadeImpl;
import logic.system.programs.repository.ProgramRepository;

public class EngineService {

    public static EngineFacade ensureEngineForUser(String username, String programName) {
        if (username == null || programName == null) return null;

        EngineFacade engine = EngineFacadeManager.getEngine(username, programName);
        if (engine == null) {
            SProgram program = ProgramRepository.getProgramByName(programName);
            if (program == null) return null;

            engine = new EngineFacadeImpl();
            engine.loadExistingProgram(program);
            EngineFacadeManager.registerEngine(username, programName, engine);
        }
        return engine;
    }
}
