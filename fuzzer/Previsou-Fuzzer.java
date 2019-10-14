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

            int lessOrMore = 0;
            ArrayList <String[]>errorProneString = new ArrayList<String[]>();

            //TODO:Since the invalid inputs will be generated as the last line, the randominisation will be creatd outside of the main loop and generate the inputs for the last line. 
            tempInsSeed=generateRandomInt(0, 5);
            // System.out.println(tempInsSeed);
            lessOrMore = generateRandomInt(0, 1);
            boolean isLess = true; 
            String inst = INSTRUCTIONS[tempInsSeed].getOpcode(); 
            
            if(lessOrMore ==0) isLess = false; 
            tempInsSeed = 0;
            switch (tempInsSeed){
              case PUT:
                System.out.println("PUT");
                numberStrings = 3; 
                if (isLess) errorProneString.add(genLessStrNum(PUT));
                else System.out.println("else");
                break;
                // pw.write();
              case GET:
                System.out.println("GET");
                numberStrings = 1;
                errorProneString.add(genLessStrNum(GET));
                break;
              case REM:
                System.out.println("REM");
                numberStrings = 1;
                errorProneString.add(genLessStrNum(REM));
                break;
              case LIST:
                System.out.println("LIST");
                numberStrings = 0;
                break;
              default:
                break;
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
      // nextInt(least, bound): bound will be exclusive so add 1. 
      return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    public static String generateRandomStr(int seed){
      return Character.toString((char) seed);
    }
    // TODO: Refactor code, reduce copy and paste. 
    private static String[] genLessStrNum(int inst){
      int tempNumArgs = 0;

      switch (inst){
        // Supposed to have three arguments. Generate 0 / 1 / 2 number of inputs. 
        case PUT:
          tempNumArgs = generateRandomInt(0, 2);
          tempNumArgs = 2;
          return splitRandomStrLess(tempNumArgs);
        case GET:
          tempNumArgs = 0;
        case REM:
          tempNumArgs = 0;
          return splitRandomStrLess(tempNumArgs);
      }
      return null;
    }
    // Generate a random string from command. 
    // Split the string randomly and it should be invalid args. 
    private static String[] splitRandomStrLess(int numArg){
      String tempString = "";
      String[] output = new String[numArg+1];
      
      int randomSeed = 0, lenOfArgs = 0, maxLength = MAX_INSTRUCTION_LENGTH-numArg;
      int splitIndex = 0;
      while(lenOfArgs < maxLength){
        randomSeed = generateRandomInt(33, 127);
            // System.out.println(randomSeed);
        tempString += generateRandomStr(randomSeed);
        lenOfArgs ++;
      }
      System.out.println(tempString.length());
      if(numArg ==0){
        tempString = " ";
        output[0] = tempString;
        return output;
      }
      // else if(numArg == 1){
      //   output[0] = tempString;
      //   return output;
      // }
      else{
        splitIndex = generateRandomInt(0, maxLength-1);
        String sub = tempString.substring(0,splitIndex);
        String remain = tempString.substring(splitIndex);
        output[0] = sub;
        output[1] = remain;
        System.out.println(tempString);
        System.out.println(sub);
        System.out.println(remain);

        System.out.println(splitMultipleStrings(numArg, tempString));
        System.out.println("Full String: \n"+tempString);
        // System.out.println(splitMultipleStrings(numArg, tempString));


        return output;
      }
    }
    private static String[] splitMultipleStrings(int numArgs, String input){
      String tempString = "", sub = ""; 
      // TODO: 这里有时候会越界？？
      int splitIndex = generateRandomInt(0, MAX_INSTRUCTION_LENGTH-numArgs), tempSplit=0;
      String[] output = new String[numArgs];
      boolean isFirstArg = true;
      System.out.println("New Method: ");
      System.out.println(input.length());
      for (int i =0; i < numArgs; i++){
        if (isFirstArg){
          tempString = input.substring(0,splitIndex);
          output[i] = tempString;
          // System.out.println(output[i]);
          isFirstArg = false;
          System.out.println(i+" Enter first one ");
          continue;
        }
        tempSplit = splitIndex;
        // New splitindex. 
        splitIndex = generateRandomInt(tempSplit, MAX_INSTRUCTION_LENGTH-numArgs);
        if(i == numArgs -1){
          tempString = input.substring(tempSplit, MAX_INSTRUCTION_LENGTH-numArgs);
          output[i] = tempString;
          System.out.println(i+" Enter last one ");
          break;
        }
        tempString = input.substring(tempSplit,splitIndex);

        output[i+1] = tempString;
        System.out.println(i+" Enter normal one ");

        // System.out.println(i+":\n"+output[i]);
      }
      // Traverse full split string. For testing purpose: 
      for (int i =0; i < numArgs; i++){
        System.out.println(i+":\n"+output[i]);
      } 

      return output;
    }

    private static String[] getMoreStrNum(int inst){
      

      return null;
    }
}
// TODO: Invalid inputs format: 1. 比如说input number 少了，input number多了。 2. 一行的string超过1022. 3. 总体instruction 数量超过 1024. 
