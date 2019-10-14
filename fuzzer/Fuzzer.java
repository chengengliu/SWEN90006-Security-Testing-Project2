import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
<<<<<<< Updated upstream
=======

/* a stub for your team's fuzzer */
public class Fuzzer {

	private static final String OUTPUT_FILE = "fuzz.txt";
//	private static final String STATUS_FILE = "status.txt";
	private static final String PROPERTIES = "../state.properties";

	private static final int TOTAL_STRATEGY = 20;
	private static final int RANDOM_SEED = 10;
	private static final int MAX_LINES = 1024;
	private static final int MAX_INSTRUCTION_LENGTH = 1022;
	private static final int INS_LENGTH = 3;
	private static final int WHITE_SPACE = 1;
	private static final int MIN_INPUT = 1;
	private static final int PUT_MAX_INPUT = MAX_INSTRUCTION_LENGTH - INS_LENGTH - WHITE_SPACE * 3 - 1 - 1;
	private static final int SAVE_MAX_INPUT = MAX_INSTRUCTION_LENGTH - (INS_LENGTH + 1) - WHITE_SPACE * 2 - 1;
	private static final int GET_REM_MAX_INPUT = MAX_INSTRUCTION_LENGTH - INS_LENGTH - WHITE_SPACE;

	private static FileOutputStream out = null;
	private static PrintWriter pw = null;
	private static Instruction[] INSTRUCTIONS = Instruction.values();

	public static void main(String[] args) throws IOException {

		ArrayList<String> shuffleContainer = new ArrayList<String>();

		System.out.println(Instruction.getBNF());

		System.out.println(generateInstructions("put", 1).get(0));
		System.out.println(insertRandomInstructions(1).get(0));

		Iterator<String> it = null;

		int state = getValue(PROPERTIES, "state");
		System.out.println("state=" + state);
		int round = state % TOTAL_STRATEGY; // module makes sure it can wrap-around
		System.out.println("round=" + round);

		try {
			out = new FileOutputStream(OUTPUT_FILE);
			pw = new PrintWriter(out);

			switch (round) {
			case 0:
				// only 1 line of instruction
				pw.println(insertRandomInstructions(1).get(0));
				break;
			case 1:
				// 1024 lines of instructions
				it = insertRandomInstructions(MAX_LINES).iterator();
				write(it);
				break;
			case 2:
				// only one node in the tree
				pw.println(generateInstructions("put", 1).get(0));
				break;
			case 3:
				// text file does not exist
				break;
			case 4:
				// ordered instructions
				it = insertOrderedInstructions(1).iterator();
				write(it);
				break;
			case 5:
				// min & max inputs
				it = insertMinMaxInstructions().iterator();
				write(it);
				break;
			case 6:
				// same puts
				it = insertSamePut(1).iterator();
				write(it);
				break;
			case 7:
				// put > 500 times
				it = generateInstructions("put", 501).iterator();
				write(it);
				break;
			case 8:
				// random instructions
				it = insertRandomInstructions(RANDOM_SEED).iterator();
				write(it);
				break;
			case 9:
				// 1024行全是put/get/rem/save...
				break;
			case 10:
				// put, rem 次数一样; put, get 次数一样...
				break;
			case 11:
				// 在1024行中，只用特定的几种url/username/password
        break;
      case 12:
        // Add last invalid line. 
			}

//			ArrayList<String> minMaxList = insertMinMaxInstructions();
//			ArrayList<String> randomList = insertRandomInstructions(RANDOM_SEED);
//			shuffleContainer.addAll(randomList);
//			shuffleContainer.addAll(minMaxList);
//			shuffleContainer.addAll(insertSamePut(1));
//			shuffleContainer.addAll(generateInstructions("get", 20));
//
//			Collections.shuffle(shuffleContainer); // randomize these inputs
//
//			it = shuffleContainer.iterator();
//			write(it);

			// make sure the first .txt only has 1 input, second .txt only has 1024 inputs
			if (round != 0 || round != 1) {
				/* insert a "list" */
				pw.println("list");
				
				/* insert an invalid input */
				pw.println(insertLongInstructions());	
			}
			
			/* update state */
			writeProperty(PROPERTIES, "state", round + 1);
			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		} finally {
			if (pw != null) {
				pw.flush();
			}
			if (out != null) {
				out.close();
			}
		}

	}

	/**
	 * Produce a random integer in [min, max]
	 * 
	 * @param min left bound
	 * @param max right bound
	 * @return random integer
	 */
	private static int generateRandomInt(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}
//
//	public static String generateRandomStr(int seed) {
//		return Character.toString((char) seed);
//	}

	/**
	 * Produce minimum and maximum length get/rem/put instructions with random
	 * string
	 * 
	 * @return list of the 7 inputs
	 */
	private static ArrayList<String> insertMinMaxInstructions() {

		String getMin = "get " + generateRandomString(MIN_INPUT);
		String getMax = "get " + generateRandomString(GET_REM_MAX_INPUT);
		String remMin = "rem " + generateRandomString(MIN_INPUT);
		String remMax = "rem " + generateRandomString(GET_REM_MAX_INPUT);
		String put1 = "put " + generateRandomString(MIN_INPUT) + " " + generateRandomString(MIN_INPUT) + " "
				+ generateRandomString(PUT_MAX_INPUT);
		String put2 = "put " + generateRandomString(MIN_INPUT) + " " + generateRandomString(PUT_MAX_INPUT) + " "
				+ generateRandomString(MIN_INPUT);
		String put3 = "put " + generateRandomString(PUT_MAX_INPUT) + " " + generateRandomString(MIN_INPUT) + " "
				+ generateRandomString(MIN_INPUT);

		String[] instructions = { getMin, getMax, remMin, remMax, put1, put2, put3 };

		return new ArrayList<String>(Arrays.asList(instructions));
	}

	/**
	 * Insert additional seed times same PUT inputs
	 * 
	 * @param seed how many times to insert. seed=1 means there are 2 same puts in
	 *             total.
	 * @return
	 */
	private static ArrayList<String> insertSamePut(int seed) {

		int count = 0;
		ArrayList<String> list = new ArrayList<String>();
		String put = generateInstructions("put", 1).get(0);

		System.out.println("Same Put: " + put);

		list.add(put);
		while (count < seed) {
			list.add(put);
			count++;
		}

		return list;
	}

	// ##### Is this necessary #####
	/**
	 * Produce ordered instructions, e.g get->rem->put, etc. with random string
	 * length
	 * 
	 * @param seed how many instructions to produce
	 * @return list of No.seed inputs
	 */
	private static ArrayList<String> insertOrderedInstructions(int seed) {

		int count = 0;
		ArrayList<String> list = new ArrayList<String>();

		while (count < seed) {
			// get -> rem -> put
			list.add(generateInstructions("get", 1).get(0));
			list.add(generateInstructions("rem", 1).get(0));
			list.add(generateInstructions("put", 1).get(0));

			count++;
		}
		count = 0;
		while (count < seed) {
			// get -> put -> rem
			list.add(generateInstructions("get", 1).get(0));
			list.add(generateInstructions("put", 1).get(0));
			list.add(generateInstructions("rem", 1).get(0));

			count++;
		}
		count = 0;
		while (count < seed) {
			// rem -> get -> put
			list.add(generateInstructions("rem", 1).get(0));
			list.add(generateInstructions("get", 1).get(0));
			list.add(generateInstructions("put", 1).get(0));

			count++;
		}
		count = 0;
		while (count < seed) {
			// rem -> put -> get
			list.add(generateInstructions("rem", 1).get(0));
			list.add(generateInstructions("put", 1).get(0));
			list.add(generateInstructions("get", 1).get(0));

			count++;
		}
		count = 0;
		while (count < seed) {
			// put -> rem -> get
			list.add(generateInstructions("put", 1).get(0));
			list.add(generateInstructions("rem", 1).get(0));
			list.add(generateInstructions("get", 1).get(0));

			count++;
		}
		count = 0;
		while (count < seed) {
			// put -> get -> rem
			list.add(generateInstructions("put", 1).get(0));
			list.add(generateInstructions("get", 1).get(0));
			list.add(generateInstructions("rem", 1).get(0));

			count++;
		}

		return list;
	}

	/**
	 * 
	 * Randomly produce get/rem/put instructions with random string length in seed times
	 * 
	 * @param seed how many instructions to produce
	 * @return list of No.seed inputs
	 */
	private static ArrayList<String> insertRandomInstructions(int seed) {

		int count = 0;
		Instruction ins;
		ArrayList<String> list = new ArrayList<String>();
>>>>>>> Stashed changes


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
