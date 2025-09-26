package logic.domain.expand.functionCall;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.basic.bNoJumpInst.NeutralInst;
import logic.domain.instructions.synthetic.sJumpInst.*;
import logic.domain.instructions.synthetic.sNoJumpInst.AssignmentInst;
import logic.domain.instructions.synthetic.sNoJumpInst.QuoteInst;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;
import java.util.*;


public final class FunctionCallExpander {

    private final ExpansionContext ctx;
    private final QuoteInst quoteInst;
    private final JumpEqualFuncInst jumpEqInst;
    private final Map<SVars,SVars> varMap=new HashMap<>();
    private final Map<SLabel,SLabel> labelMap=new HashMap<>();

    // --- Constructors ---
    public FunctionCallExpander(ExpansionContext ctx, QuoteInst quoteInst) {
        this.ctx = ctx;
        this.quoteInst = quoteInst;
        this.jumpEqInst = null;
    }

    public FunctionCallExpander(ExpansionContext ctx, JumpEqualFuncInst jefInst) {
        this.ctx = ctx;
        this.quoteInst = null;
        this.jumpEqInst = jefInst;
    }

    // --- Expansion for QUOTE ---
    public List<SInstruction> expandQuote() {
        List<SInstruction> out = new ArrayList<>();

        if (!quoteInst.getLabel().equals(SpecialLabels.EMPTY)) {
            out.add(new NeutralInst(quoteInst.getVariable(), quoteInst.getLabel()));
        }

        String funcName = quoteInst.getFunctionName();
        List<SVars> formalParams = ctx.getFunctionLookup().argsOf(funcName);
        List<SInstruction> body = ctx.lookupFunctionBody(funcName);

        Map<SVars, SVars> varMap = new HashMap<>();
        Map<SLabel, SLabel> labelMap = new HashMap<>();

        // --- Map EXIT → Lend ---
        SLabel lendLabel = ctx.newFreeLabel();
        labelMap.put(SpecialLabels.EXIT, lendLabel);

        // --- Prepare arguments ---
        for (int i = 0; i < formalParams.size(); i++) {
            if (i >= quoteInst.getArguments().size()) break;

            SVars formal = formalParams.get(i);
            SVars zFormal = ctx.newWorkVar();
            varMap.put(formal, zFormal);

            SVars resolved = ctx.resolveArgument(quoteInst.getArguments().get(i), out);
            out.add(new AssignmentInst(zFormal, resolved));
        }

        // --- Result variable mapping ---
        SVars funcResult = ctx.lookupFunctionResult(funcName);
        SVars zOut = ctx.newWorkVar();
        varMap.put(funcResult, zOut);

        // --- Copy body with remap ---
        for (SInstruction ins : body) {
            SInstruction cloned = ins.remap(varMap, labelMap);
            out.add(cloned);
        }

        // --- Add final assignment with Lend ---
        out.add(new AssignmentInst(quoteInst.getVariable(), zOut, lendLabel));
        return out;
    }


    // --- Expansion for JUMP_EQUAL_FUNCTION ---
    public List<SInstruction> expandJumpEqualFunc() {
        List<SInstruction> out = new ArrayList<>();

        if (!jumpEqInst.getLabel().equals(SpecialLabels.EMPTY)) {
            out.add(new NeutralInst(jumpEqInst.getVariable()));
        }

        SVars tempResult = ctx.newWorkVar();
        String funcName = jumpEqInst.getFunctionName();
        List<SVars> formalParams = ctx.getFunctionLookup().argsOf(funcName);
        List<SInstruction> body = ctx.lookupFunctionBody(funcName);

        Map<SVars, SVars> varMap = new HashMap<>();
        Map<SLabel, SLabel> labelMap = new HashMap<>();

        for (int i = 0; i < formalParams.size(); i++) {
            if (i >= jumpEqInst.getFunctionArgs().size()) break;
            SVars formal = formalParams.get(i);
            SVars zFormal = ctx.newWorkVar();
            varMap.put(formal, zFormal);

            SVars resolved = ctx.resolveArgument(jumpEqInst.getFunctionArgs().get(i), out);
            out.add(new AssignmentInst(zFormal, resolved));
        }

        SVars funcResult = ctx.lookupFunctionResult(funcName);
        varMap.put(funcResult, tempResult);

        for (SInstruction ins : body) {
            out.add(ins.remap(varMap, labelMap));
        }

        out.add(new JumpEqualVariableInst(jumpEqInst.getVariable(), tempResult, jumpEqInst.getTargetLabel()));
        return out;
    }

    // --- Getters for function data ---
    private String getFuncName() {
        return quoteInst != null ? quoteInst.getFunctionName() : jumpEqInst.getFunctionName();
    }
}


