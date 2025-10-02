package logic.domain.expand.functionCall;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.domain.instructions.basic.bNoJumpInst.NeutralInst;
import logic.domain.instructions.synthetic.sJumpInst.*;
import logic.domain.instructions.synthetic.sNoJumpInst.AssignmentInst;
import logic.domain.instructions.synthetic.sNoJumpInst.QuoteInst;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;
import java.util.*;


public final class FunctionCallExpander {

    private final ExpansionContext ctx;     // expansion tools
    private final QuoteInst quoteInst;
    private final JumpEqualFuncInst jumpEqInst;

    // ==== Constructors ====
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

    // ==== Expansion for QUOTE ====
    public List<SInstruction> expandQuote() {
        List<SInstruction> out = new ArrayList<>();

        if (!quoteInst.getLabel().equals(SpecialLabels.EMPTY)) {
            out.add(new NeutralInst(quoteInst.getVariable(), quoteInst.getLabel())); // preserve original label
        }

        String funcName = quoteInst.getFunctionName();
        List<SVars> formalParams = ctx.getFunctionLookup().argsOf(funcName);
        List<SInstruction> body = ctx.lookupFunctionBody(funcName);

        Map<SVars, SVars> varMap = new HashMap<>();   // formal→fresh z
        Map<SLabel, SLabel> labelMap = new HashMap<>();  // L#→fresh L#

        // Map EXIT → fresh label
        mapExitLabel(labelMap);

        // Bind formal params to temporaries  (and emit assignments from resolved args)
        for (int i = 0; i < formalParams.size(); i++) {
            if (i >= quoteInst.getArguments().size()) break;
            SVars formal = formalParams.get(i);     // x1..xK of function
            SVars zFormal = ctx.newWorkVar();       // allocate z tmp
            varMap.put(formal, zFormal);            // map x_i → zN

            SVars resolved = ctx.resolveArgument(quoteInst.getArguments().get(i), out);
            out.add(new AssignmentInst(zFormal, resolved));   // zFormal = resolved
        }

        // Map function result (y) to a fresh z
        SVars funcResult = ctx.lookupFunctionResult(funcName);
        SVars zOut = ctx.newWorkVar();
        varMap.put(funcResult, zOut);

        // Remap numeric labels & work vars used inside body
        mapNumericLabels(body, labelMap);
        mapWorkVars(body, varMap);

        // Clone function body with remapping
        for (SInstruction ins : body) {
            SInstruction cloned = ins.remap(varMap, labelMap);
            out.add(cloned);
        }

        // Assign final result into caller target var, using mapped EXIT
        out.add(new AssignmentInst(quoteInst.getVariable(), zOut, labelMap.get(SpecialLabels.EXIT)));
        return out;
    }


    // ==== Expansion for JUMP_EQUAL_FUNCTION ====
    public List<SInstruction> expandJumpEqualFunc() {
        List<SInstruction> out = new ArrayList<>();

        if (!jumpEqInst.getLabel().equals(SpecialLabels.EMPTY)) {
            out.add(new NeutralInst(jumpEqInst.getVariable()));  // preserve label position
        }

        SVars tempResult = ctx.newWorkVar();  // zN temp
        out.add(new QuoteInst(tempResult, jumpEqInst.getFunctionName(), jumpEqInst.getFunctionArgs())); // compute temp
        out.add(new JumpEqualVariableInst(jumpEqInst.getVariable(), tempResult, jumpEqInst.getTargetLabel())); // compare
        return out;
    }


    private void mapExitLabel(Map<SLabel, SLabel> labelMap) {
        labelMap.put(SpecialLabels.EXIT, ctx.newFreeLabel());  // EXIT → fresh L#
    }

    private void mapNumericLabels(List<SInstruction> body, Map<SLabel, SLabel> labelMap) {
        Set<SLabel> numericLabels = new HashSet<>();
        for (SInstruction ins : body) {
            SLabel lbl = ins.getLabel();
            if (lbl != null && lbl.isNumberLabel()) numericLabels.add(lbl);

            // Collect targets that are numeric labels
            if (ins instanceof JumpZeroInst jz && jz.getTargetLabel().isNumberLabel())
                numericLabels.add(jz.getTargetLabel());
            if (ins instanceof JumpEqualConstantInst jec && jec.getTargetLabel().isNumberLabel())
                numericLabels.add(jec.getTargetLabel());
            if (ins instanceof JumpEqualVariableInst jev && jev.getTargetLabel().isNumberLabel())
                numericLabels.add(jev.getTargetLabel());
            if (ins instanceof GoToLabelInst go && go.getTargetLabel().isNumberLabel())
                numericLabels.add(go.getTargetLabel());
            if (ins instanceof JumpNotZeroInst jnz && jnz.getTargetLabel().isNumberLabel())
                numericLabels.add(jnz.getTargetLabel());
            if (ins instanceof JumpEqualFuncInst jef && jef.getTargetLabel().isNumberLabel())
                numericLabels.add(jef.getTargetLabel());
        }
        for (SLabel oldLbl : numericLabels) {
            if (oldLbl != SpecialLabels.EXIT && !labelMap.containsKey(oldLbl)) {
                labelMap.put(oldLbl, ctx.newFreeLabel());    // Lk → new Lm
            }
        }
    }

    private void mapWorkVars(List<SInstruction> body, Map<SVars, SVars> varMap) {
        Set<SVars> workVars = new HashSet<>();
        for (SInstruction ins : body) {

            // collect targets/sources of type WORK
            SVars tgt = ins.getVariable();
            if (tgt != null && tgt.getType() == logic.domain.variable.SVarsType.WORK) workVars.add(tgt);
            if (ins instanceof AssignmentInst asg) {
                SVars src = asg.getSourceVar();
                if (src != null && src.getType() == logic.domain.variable.SVarsType.WORK) {
                    workVars.add(src);
                }
            }
            if (ins instanceof JumpEqualVariableInst jev) {
                SVars other = jev.getOtherVar();
                if (other != null && other.getType() == logic.domain.variable.SVarsType.WORK) workVars.add(other);
            }
        }
        for (SVars w : workVars) {      // ensure each WORK var has a fresh z
            varMap.putIfAbsent(w, ctx.newWorkVar());
        }
    }
}


