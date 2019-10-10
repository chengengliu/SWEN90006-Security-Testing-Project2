import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import java.util.concurrent.ThreadLocalRandom;


/* a stub for your team's fuzzer */
public class Fuzzer {

    private static final String OUTPUT_FILE = "fuzz.txt";
    private static int commandNum = 0;
    private static final String STATUS_FILE = "status.txt";

    private final static int MAX_LINES = 1024;
    private final static int MAX_INSTRUCTION_LENGTH = 1022; 
    private static Instruction[] INSTRUCTIONS = Instruction.values();

    private final static int PUT = 0;
    private final static int GET = 1;
    private final static int REM = 2;
    private final static int SAVE = 3;
    private final static int LIST = 4;
    private final static int MASTERPW = 5;
    
    public static void main(String[] args) throws IOException {
        System.out.println(Instruction.getBNF());
        FileOutputStream out = null;
        PrintWriter pw = null;

        Integer runtime; 
        try {
            out = new FileOutputStream(OUTPUT_FILE);
            pw = new PrintWriter(out);

            //TODO: 这两行将来放到loop里，生成随机char。 
            // int seed = generateRandomInt(0, 127);
            // String ascii = generateRandomStr(seed);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(STATUS_FILE), "utf-8"));
            int status = Integer.parseInt(bufferedReader.readLine());



            // pw.print("put ");
            // for (int i =0; i < 1025; i++){
            //   pw.print(i);
            // }
            
            /* We just print one instruction.
               Hint: you might want to make use of the instruction
               grammar which is effectively encoded in Instruction.java */
            // pw.println();
            // pw.println("list");
            // pw.println("put http://www.google.com");

            // pw.println();

            boolean notListCommand = true;

            System.out.println(INSTRUCTIONS);
            System.out.println(INSTRUCTIONS[0]);
            int numberLines = 0, tempInsSeed = 0, numberStrings = 0; 
            
            while(numberLines<1022){
              tempInsSeed=generateRandomInt(0, 5);

              String inst = INSTRUCTIONS[tempInsSeed].getOpcode();  
              switch (tempInsSeed){
                case PUT:
                  numberStrings = 3; 
                case GET:
                  numberStrings = 1;
                case REM:
                  numberStrings = 1;
                case SAVE:
                  numberStrings = 2;
                case LIST:
                  numberStrings = 0;
                case MASTERPW:
                  numberStrings = 1;
              }            
              numberLines++;
            }
            
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
    // Add some randominisation. 
    
    public static int generateRandomInt(int min, int max){
      return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    public static String generateRandomStr(int seed){
      return Character.toString((char) seed);
    }

}
