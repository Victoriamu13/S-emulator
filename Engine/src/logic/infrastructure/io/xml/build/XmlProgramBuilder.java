package logic.infrastructure.io.xml.build;

import logic.domain.instructions.SInstruction;
import logic.domain.instructions.synthetic.sJumpInst.JumpEqualFuncInst;
import logic.domain.instructions.synthetic.sNoJumpInst.QuoteInst;
import logic.domain.program.functions.FunctionRepository;
import logic.domain.program.info.ProgramInfoUtils;
import logic.domain.variable.SVars;
import logic.infrastructure.io.xml.dto.RawFunction;
import logic.infrastructure.io.xml.dto.RawInstructions;
import logic.domain.program.SProgram;
import logic.domain.program.SProgramImpl;
import logic.infrastructure.io.xml.parser.composition.ComposeArgument;
import logic.infrastructure.io.xml.parser.composition.FuncCallArgument;
import logic.infrastructure.io.xml.parser.composition.VarArgument;

import java.util.*;

import static logic.infrastructure.io.xml.build.BuildUtils.*;
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
            String fnUserString = (fn.userString() == null || fn.userString().isBlank())
                    ? fnName
                    : fn.userString().trim();
            List<RawInstructions> bodyRaw = safeList(fn.body());

            SProgram tmp = build("[fn] " + fnName, bodyRaw);

            List<SInstruction> bodyCopy = new ArrayList<>(tmp.getInstructions());

            Set<String> inNames = new LinkedHashSet<>(ProgramInfoUtils.inputsUsed(bodyCopy));

            for (SInstruction ins : bodyCopy) {
                if (ins instanceof QuoteInst q) {
                    inNames.addAll(collectVarsFromArgs(q.getArguments()));
                }
                if (ins instanceof JumpEqualFuncInst jef) {
                    inNames.addAll(collectVarsFromArgs(jef.getFunctionArgs()));
                }
            }
            List<SVars> args = inNames.stream()
                    .map(BuildUtils::buildVar)
                    .toList();
            repo.register(fnName,fnUserString, args, bodyCopy);
        }
    }

    private Set<String> collectVarsFromArgs(List<ComposeArgument> args) {
        Set<String> out = new LinkedHashSet<>();
        for (ComposeArgument a : args) {
            if (a instanceof VarArgument v) {
                out.add(v.getName());
            } else if (a instanceof FuncCallArgument f) {
                out.addAll(collectVarsFromArgs(f.getArguments()));
            }
        }
        return out;
    }
}

