import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintWriter;


/* a stub for your team's fuzzer */
public class Fuzzer {

    private static final String OUTPUT_FILE = "fuzz.txt";
    
    public static void main(String[] args) throws IOException {
        System.out.println(Instruction.getBNF());
        FileOutputStream out = null;
        PrintWriter pw = null;
        try {
            out = new FileOutputStream(OUTPUT_FILE);
            pw = new PrintWriter(out);
            
            /* We just print one instruction.
               Hint: you might want to make use of the instruction
               grammar which is effectively encoded in Instruction.java */
            pw.println("list");
            
        }catch (Exception e){
            e.printStackTrace(System.err);
            System.exit(1);
        }finally{
            if (pw != null){
                pw.flush();
            }
            if (out != null){
                out.close();
            }
        }

    }

}
