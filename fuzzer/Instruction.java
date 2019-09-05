import java.util.Arrays;
import java.util.ArrayList;

public enum Instruction {
    PUT("put",new OperandType[]{OperandType.STRING,OperandType.STRING,OperandType.STRING}),
    GET("get",new OperandType[]{OperandType.STRING}),
    REM("rem",new OperandType[]{OperandType.STRING}),
    SAVE("save",new OperandType[]{OperandType.STRING,OperandType.STRING}),
    LIST("list",new OperandType[]{}),
    MASTERPW("masterpw",new OperandType[]{OperandType.STRING});

    public static String getBNF(){
        String grammar = "<INSTRUCTION> ::= \n";
        Instruction[] INSTS = Instruction.values();
        boolean firstInst = true;
        for (Instruction inst : INSTS){
            if (firstInst){
                grammar += "      \"";
                firstInst = false;
            }else{
                grammar += "    | \"";
            }
            grammar += inst.getOpcode() + "\"";
            for (OperandType op : inst.getOperands()){
                grammar += " <" + op.toString() + ">";
            }
            grammar += "\n";
        }
        return grammar;
    }
    
    private final String opcode;
    private final OperandType[] operands;

    Instruction(String opcode, OperandType[] operands){
        this.opcode = opcode;
        this.operands = operands;
    }

    public String getOpcode(){
        return opcode;
    }
    
    public OperandType[] getOperands(){
        return operands;
    }

    public String toString(){
        String operandsString = "";
        for (OperandType op : operands) {
            operandsString += " " + op.toString();
        }
        return "\"" + opcode + "\"" + operandsString;
    }
    
}
