package logic.infrastructure.io.xml.build;

import logic.domain.instructions.SInstruction;
import logic.domain.program.functions.FunctionLookup;
import logic.domain.program.functions.FunctionRepository;
import logic.infrastructure.io.xml.dto.RawFunction;
import logic.infrastructure.io.xml.dto.RawInstructions;
import logic.domain.program.SProgram;
import logic.domain.program.SProgramImpl;
import logic.infrastructure.io.xml.parser.utils.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.*;

import static logic.infrastructure.io.xml.build.BuildUtils.*;
import static logic.infrastructure.io.xml.parser.utils.DomUtils.allArgs;
import static logic.infrastructure.io.xml.parser.utils.DomUtils.childText;
import static logic.infrastructure.io.xml.validation.ValidationUtils.safeList;

public class XmlProgramBuilder {

    public SProgram build(String programName, List<RawInstructions> raw) {
        SProgramImpl program = new SProgramImpl(programName.trim());
        for (RawInstructions r : raw) {
            SInstruction inst = buildInstruction(r);
            program.addInstruction(inst);
        }
        return program;
    }

    public void registerFunctions(List<RawFunction> functions, FunctionRepository repo) {
        if (functions == null || functions.isEmpty()) return;

        for (RawFunction fn : functions) {
            String fnName = (fn.name() == null) ? "" : fn.name().trim();
            List<RawInstructions> bodyRaw = safeList(fn.body());

            SProgram tmp = build("[fn] " + fnName, bodyRaw);

            var bodyCopy = new ArrayList<>(tmp.getInstructions());
            repo.register(fnName, bodyCopy);
        }
    }
}

