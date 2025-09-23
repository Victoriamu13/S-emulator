package logic.domain.instructions.synthetic.sNoJumpInst.quoteInst;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.domain.instructions.data.ArgumentData;
import logic.domain.instructions.data.InstructionData;
import logic.domain.instructions.synthetic.sJumpInst.GoToLabelInst;
import logic.domain.instructions.synthetic.sJumpInst.JumpEqualConstantInst;
import logic.domain.instructions.synthetic.sJumpInst.JumpEqualVariableInst;
import logic.domain.instructions.synthetic.sJumpInst.JumpZeroInst;
import logic.domain.instructions.synthetic.sNoJumpInst.AssignmentInst;
import logic.domain.instructions.synthetic.sNoJumpInst.ConstantAssignmentInst;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;
import logic.infrastructure.io.xml.build.BuildUtils;
import logic.infrastructure.io.xml.parser.composition.ComposeArgument;
import logic.infrastructure.io.xml.parser.composition.FuncCallArgument;
import logic.infrastructure.io.xml.parser.composition.VarArgument;
import logic.infrastructure.io.xml.validation.ValidationUtils;

import java.util.*;
import java.util.stream.Collectors;

import static logic.infrastructure.io.xml.build.BuildUtils.constructInstruction;


public final class QuoteMapper {

    private final QuoteContext qctx;
    private final Map<SVars,SVars> varMap=new HashMap<>();
    private final Map<SLabel,SLabel> labelMap=new HashMap<>();
    private final SVars tempResult;
    private final SLabel endLabel;

    public QuoteMapper(List<SInstruction> quotedProgram, QuoteContext qctx){
        this.qctx=qctx;
        ExpansionContext ctx=qctx.ctx();

        for (String inputName : qctx.originInputs()) {
            SVars xi = BuildUtils.buildVar(inputName);
            SVars zi = ctx.newWorkVar();
            varMap.put(xi, zi);
        }

        this.tempResult=ctx.newWorkVar();
        varMap.put(SVars.RESULT,tempResult);

        for(SInstruction inst:quotedProgram){
            SLabel lbl=inst.getLabel();
            if(lbl.isNumberLabel()){
                labelMap.put(lbl,ctx.newFreeLabel());
            }
        }
        this.endLabel=ctx.newFreeLabel();
    }

    public List<SInstruction> buildArgumentAssignments(){
        List<SInstruction> assigns=new ArrayList<>();
        List<SVars> args = qctx.originArgs();
        List<String> inputs = new ArrayList<>(qctx.originInputs());

        inputs.sort(Comparator.comparingInt(ValidationUtils::parseXIndex));

        for (int i = 0; i < inputs.size(); i++) {
            SVars xi = BuildUtils.buildVar(inputs.get(i));
            SVars zi = varMap.get(xi);
            assigns.add(new AssignmentInst(zi,args.get(i)));
        }
        return assigns;
    }

    public SInstruction buildResultAssignment(){
        return new AssignmentInst(qctx.targetVar(),tempResult,endLabel);
    }

    public SInstruction remapInstruction(SInstruction inst){
        InstructionData type=InstructionData.valueOf(inst.getName());

        SVars mappedVar=(inst.getVariable()!=null)
                ? varMap.getOrDefault(inst.getVariable(), inst.getVariable()) : null;

        SLabel mappedLabel = (inst.getLabel() != null)
              ? labelMap.getOrDefault(inst.getLabel(), inst.getLabel()) : SpecialLabels.EMPTY;

        if (inst instanceof QuoteInst q) {
            List<ComposeArgument> mappedAst = mapComposeAst(q.getArguments());
            return new QuoteInst(mappedVar, q.getFunctionName(), mappedAst, mappedLabel);
        }

        Map<ArgumentData,String> args = remapArgs(inst);
        return constructInstruction(type, mappedVar, mappedLabel, args);
    }

    private Map<ArgumentData,String> remapArgs(SInstruction inst) {
        Map<ArgumentData,String> out = new EnumMap<>(ArgumentData.class);

        // ----- Assignment -----
        if (inst instanceof AssignmentInst asg) {
            SVars mappedSrc = varMap.getOrDefault(asg.getSourceVar(), asg.getSourceVar());
            out.put(ArgumentData.ASSIGNED_VARIABLE, mappedSrc.getRepresentation());
        }

        // ----- Constant Assignment -----
        if (inst instanceof ConstantAssignmentInst cst) {
            out.put(ArgumentData.CONSTANT_VALUE, Long.toString(cst.getConstantValue()));
        }

        // ----- Jump Not Zero -----
        if (inst instanceof JumpNotZeroInst jnz) {
            SLabel mappedTgt = mapTargetLabel(jnz.getTargetLabel());
            out.put(ArgumentData.JNZ_LABEL, mappedTgt.getLabelRepresentation());
        }

        // ----- Jump Zero -----
        if (inst instanceof JumpZeroInst jz) {
            SLabel mappedTgt = mapTargetLabel(jz.getTargetLabel());
            out.put(ArgumentData.JZ_LABEL, mappedTgt.getLabelRepresentation());
        }

        // ----- Jump Equal Constant -----
        if (inst instanceof JumpEqualConstantInst jec) {
            SLabel mappedTgt = mapTargetLabel(jec.getTargetLabel());
            out.put(ArgumentData.JE_CONSTANT_LABEL, mappedTgt.getLabelRepresentation());
            out.put(ArgumentData.CONSTANT_VALUE, Long.toString(jec.getConstantValue()));
        }

        // ----- Jump Equal Variable -----
        if (inst instanceof JumpEqualVariableInst jev) {
            SVars mappedOther = varMap.getOrDefault(jev.getOtherVar(), jev.getOtherVar());
            out.put(ArgumentData.VARIABLE_NAME, mappedOther.getRepresentation());

            SLabel mappedTgt = mapTargetLabel(jev.getTargetLabel());
            out.put(ArgumentData.JE_VARIABLE_LABEL, mappedTgt.getLabelRepresentation());
        }

        // ----- GoTo -----
        if (inst instanceof GoToLabelInst go) {
            SLabel mappedTgt = mapTargetLabel(go.getTargetLabel());
            out.put(ArgumentData.GOTO_LABEL, mappedTgt.getLabelRepresentation());
        }

        // ----- Quote -----
        if (inst instanceof QuoteInst q) {
            out.put(ArgumentData.FUNCTION_NAME, q.getFunctionName());

            String joined = q.getArguments().stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));

            out.put(ArgumentData.FUNCTION_ARGUMENTS, joined);
        }

        return out;
    }

    private SLabel mapTargetLabel(SLabel target) {
        if (target == SpecialLabels.EXIT) {
            return endLabel;
        }
        return labelMap.getOrDefault(target, target);
    }

    private List<ComposeArgument> mapComposeAst(List<ComposeArgument> ast) {
        if (ast == null || ast.isEmpty()) return List.of();
        List<ComposeArgument> out = new ArrayList<>(ast.size());
        for (ComposeArgument a : ast) out.add(mapOneArg(a));
        return out;
    }

    private ComposeArgument mapOneArg(ComposeArgument a) {
        if (a instanceof VarArgument v) {

            SVars orig = BuildUtils.buildVar(v.getName());
            SVars mapped = varMap.getOrDefault(orig, orig);
            return new VarArgument(mapped.getRepresentation());
        }
        if (a instanceof FuncCallArgument f) {
            List<ComposeArgument> mappedChildren = new ArrayList<>(f.getArguments().size());
            for (ComposeArgument child : f.getArguments()) {
                mappedChildren.add(mapOneArg(child));
            }
            return new FuncCallArgument(f.getFunctionName(), mappedChildren);
        }
        return a; // fallback
    }
}


