import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
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
        // System.out.println(Instruction.getBNF());
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


            int numberLines = 0, tempInsSeed = 0, numberStrings = 0;
            ArrayList <String[]>outputString = new ArrayList<String[]>();

            //TODO:Since the invalid inputs will be generated as the last line, the randominisation will be creatd outside of the main loop and generate the inputs for the last line. 
            while(numberLines<5){
              tempInsSeed=generateRandomInt(0, 5);
              String inst = INSTRUCTIONS[tempInsSeed].getOpcode();  
              switch (tempInsSeed){
                case PUT:
                  numberStrings = 3; 
                  outputString.add(genLessStrNum(PUT));
                  break;
                  // pw.write();
                case GET:
                  numberStrings = 1;
                  break;
                case REM:
                  numberStrings = 1;
                  break;
                case LIST:
                  numberStrings = 0;
                  break;
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
    // TODO: Refactor code, reduce copy and paste. 
    public static String[] genLessStrNum(int inst){
      int tempNumArgs = 0, lenOfArgs = 0, randomSeed = 0;
      int split1=0, split2= 0;

      int maxLength = 0;
      String tempString="";

      switch (inst){
        // Supposed to have three arguments. Generate 0 / 1 / 2 number of inputs. 
        case PUT:
          tempNumArgs = generateRandomInt(0, 2);
          System.out.println(tempNumArgs);
          //TODO: 这里需要-3么？？？需要减去split的空格么？？？
          maxLength = MAX_INSTRUCTION_LENGTH-tempNumArgs - 3;

          String[] returnString = new String[tempNumArgs+1];

          // Generate a random string without space. 
          while(lenOfArgs < maxLength){
            randomSeed = generateRandomInt(33, 126);
            // System.out.println(randomSeed);
            tempString += generateRandomStr(randomSeed);
            lenOfArgs ++;
          }
          // Split the string according to the number of args. 
          // Return a null string.
          if(tempNumArgs ==0){
            tempString = " ";
            returnString[0]= tempString;
            return returnString;
          }
          // Not split.
          else if (tempNumArgs == 1){
            returnString[0] = tempString;
            return returnString;
          }
          // Split once. 
          else{
            split1 = generateRandomInt(0, maxLength-1);
            String sub = tempString.substring(0,split1);
            String remain = tempString.substring(split1);
            returnString[0] = sub;
            returnString[1] = remain;
            System.out.println(sub);
            System.out.println(remain);
            return returnString;

          }
      }
      return null;
    }
    // Generate a random string from command. 
    // Split the string randomly and it should be invalid args. 
    private static String splitRandomStr(int numSplit, int command){
      return null; 
    }
}
// TODO: Invalid inputs format: 1. 比如说input number 少了，input number多了。 2. 一行的string超过1022. 3. 总体instruction 数量超过 1024. 
