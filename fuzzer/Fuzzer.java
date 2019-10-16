import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/* a stub for your team's fuzzer */
public class Fuzzer {

	private static final String OUTPUT_FILE = "fuzz.txt";
	// private static final String STATUS_FILE = "status.txt";
	private static final String PROPERTIES = "../state.properties";

	private static final int TOTAL_STRATEGY = 23;
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

		Iterator<String> it = null;

		int state = getValue(PROPERTIES, "state");
		System.out.println("state=" + state);
		int round = state % TOTAL_STRATEGY; // module makes sure it can wrap-around
		System.out.println("round=" + round);

		int putNum = 0, getNum = 0, remNum = 0, saveNum = 0, listNum = 0;
		String get = "";
		String put = "";
		String rem = "";
		String save = "";

		String invalidString = "";
		int numOfArg = 0;

		try {
			out = new FileOutputStream(OUTPUT_FILE);
			pw = new PrintWriter(out);

			switch (round) {
			case 0:
				// do not insert any node in the tree (not using PUT)
				listNum = generateRandomInt(1, 2);
				getNum = generateRandomInt(1, MAX_LINES - 1 - listNum);
				remNum = generateRandomInt(1, MAX_LINES - 1 - listNum - getNum);

				shuffleContainer.addAll(insertLists(listNum));
				shuffleContainer.addAll(generateInstructions("get", getNum));
				shuffleContainer.addAll(generateInstructions("rem", remNum));
				shuffleContainer.add(insertSaves(1).get(0)); // insert 1 save instruction

				Collections.shuffle(shuffleContainer);
				it = shuffleContainer.iterator();
				write(it);
				// invalid: get with zero input argument.
				invalidString = generateInvalidInstructions(0, 0, "get");
				pw.println(invalidString);
				break;

			case 1:
				// only insert one node in the tree (put once)
				put = generateInstructions("put", 1).get(0);
				get = "get " + getURL(put);
				rem = "rem " + getURL(put);
				save = generateInstructions("save", 1).get(0);

				pw.println(put);
				pw.println(get);
				pw.println(rem);
				pw.println("list");
				pw.println(save);

				listNum = generateRandomInt(1, 2);
				getNum = generateRandomInt(1, MAX_LINES - 5 - 1 - listNum);
				remNum = generateRandomInt(1, MAX_LINES - 5 - 1 - listNum - getNum);

				// shuffleContainer.addAll(insertLists(listNum));
				shuffleContainer.addAll(generateInstructions("get", getNum));
				shuffleContainer.addAll(generateInstructions("rem", remNum));
				// shuffleContainer.addAll(generateInstructions("save", saveNum));

				Collections.shuffle(shuffleContainer);
				it = shuffleContainer.iterator();
				write(it);

				pw.println(insertLongInstructions()); // invalid: long instruction > 1022
				break;
			case 2:
				// 0 line of instruction (empty file)
				// do nothing
				break;
			case 3:
				// min & max inputs
				shuffleContainer.addAll(insertRandomInstructions(MAX_LINES - 1 - 9 - 1));
				shuffleContainer.addAll(insertMinMaxInstructions());
				Collections.shuffle(shuffleContainer);
				it = shuffleContainer.iterator();
				write(it);

				// invalid: get with two input arguments. (maybe later randomnise and generate
				// two or more input arguments? )
				// numOfArg = generateRandomInt(2, 1022);
				// invalidString = generateInvalidInstructions(MAX_INSTRUCTION_LENGTH - 3 - numOfArg, numOfArg - 1, "get");
				invalidString = "get " + generateRandomString(200) + " " + generateRandomString(200);
				// minus three chars of instruction, 2 whitespaec.
				pw.println(invalidString);
				break; 
			case 4:
				// only 1 line of instruction
				pw.println(insertRandomInstructions(1).get(0));
				break;
			case 5:
				// 1024 lines of instructions
				it = insertRandomInstructions(MAX_LINES).iterator();
				write(it);
				break;
			case 6:
				// insert random instructions random times
				putNum = generateRandomInt(1, (MAX_LINES - 1 - 3) / 5);
				// getNum = generateRandomInt(1, MAX_LINES - 1 - listNum);
				// remNum = generateRandomInt(1, MAX_LINES - 1 - listNum - getNum);
				// saveNum = MAX_LINES - 1 - listNum - getNum - remNum;

				shuffleContainer.addAll(insertSameInstructions("put", putNum));
				shuffleContainer.addAll(insertSameInstructions("get", putNum));
				shuffleContainer.addAll(insertSameInstructions("rem", putNum));
				shuffleContainer.addAll(insertSaves(putNum));

				Collections.shuffle(shuffleContainer);
				it = shuffleContainer.iterator();
				write(it);
				// invalid: rem with 0 input argument.
				invalidString = generateInvalidInstructions(0, 0, "rem");
				pw.println(invalidString);
				break;
			case 7:
				// 1024 lines of PUT
				it = generateInstructions("put", MAX_LINES).iterator();
				write(it);
				break;
			case 8:
				// 1024 lines of GET
				it = generateInstructions("get", MAX_LINES).iterator();
				write(it);
				break;
			case 9:
				// 1024 lines of REM
				it = generateInstructions("rem", MAX_LINES).iterator();
				write(it);
				break;
			case 10:
				//  > 1024 lines of random instructions
				it = insertRandomInstructions(generateRandomInt(MAX_LINES, 2*MAX_LINES)).iterator();
				write(it);
				break;
			case 11:
				// instructions consist of all numbers
				shuffleContainer.addAll(insertNumbersInstructions());
				Collections.shuffle(shuffleContainer);
				it = shuffleContainer.iterator();
				write(it);
				
				// invalid: save with 0 args
				invalidString = generateInvalidInstructions(0, 0, "save");
				pw.println(invalidString);
				break;
			case 12: 
				// instructions consist of a-z, A-Z (alphabet)
				shuffleContainer.addAll(insertAlphabetInstructions());
				Collections.shuffle(shuffleContainer);
				it = shuffleContainer.iterator();
				write(it);

				// invalid: save with 1 arg
				invalidString = generateInvalidInstructions(MAX_INSTRUCTION_LENGTH - 4 - 1, 0, "save");
				pw.println(invalidString);
			case 13:
				// PUT, REM same times
				shuffleContainer.addAll(generateInstructions("put", MAX_LINES / 2));
				shuffleContainer.addAll(generateInstructions("rem", MAX_LINES / 2));

				Collections.shuffle(shuffleContainer);
				it = shuffleContainer.iterator();
				write(it);
				break;
			case 14:
				// PUT, GET same times
				shuffleContainer.addAll(generateInstructions("put", MAX_LINES / 2));
				shuffleContainer.addAll(generateInstructions("get", MAX_LINES / 2));

				Collections.shuffle(shuffleContainer);
				it = shuffleContainer.iterator();
				write(it);
				break;
			case 15:
				// REM, GET same times
				shuffleContainer.addAll(generateInstructions("rem", MAX_LINES / 2));
				shuffleContainer.addAll(generateInstructions("get", MAX_LINES / 2));

				Collections.shuffle(shuffleContainer);
				it = shuffleContainer.iterator();
				write(it);
				break;
			case 16:
				// REM, GET, PUT same times
				shuffleContainer.addAll(generateInstructions("rem", (MAX_LINES - 1) / 3));
				shuffleContainer.addAll(generateInstructions("get", (MAX_LINES - 1) / 3));
				shuffleContainer.addAll(generateInstructions("put", (MAX_LINES - 1) / 3));

				Collections.shuffle(shuffleContainer);
				it = shuffleContainer.iterator();
				write(it);
				// invalid: rem with two (or more) instructions.
				// numOfArg = generateRandomInt(2, 1022);
				// invalidString = generateInvalidInstructions(MAX_INSTRUCTION_LENGTH - 3 - numOfArg, numOfArg - 1, "rem");
				invalidString = "rem " + generateRandomString(200) + " " + generateRandomString(200);
				pw.println(invalidString);
				break;
			case 17:
				// same URL, different username/password
				String url = getURL(generateInstructions("put", 1).get(0));

				get = "get " + url;
				rem = "rem " + url;

				int n = 0;
				int remainLength = MAX_INSTRUCTION_LENGTH - INS_LENGTH - WHITE_SPACE * 3 - url.length();
				while (n < 1010) {
					String temp = generateRandomString(generateRandomInt(2, remainLength));
					shuffleContainer.add("put " + url + " " + randomSplit(temp, 1));
					n++;
				}
				shuffleContainer.add(get);
				shuffleContainer.add(get);
				shuffleContainer.add(get);

				Collections.shuffle(shuffleContainer);
				it = shuffleContainer.iterator();
				write(it);

				// pw.println("list");
				pw.println(rem);
				break;
			case 18:
				// ordered instructions
				it = insertOrderedInstructions(1).iterator();
				write(it);
				// invalid: put with 0 argument
				invalidString = generateInvalidInstructions(0, 0, "put");
				pw.println(invalidString);
				break;
			case 19:
				// random instructions
				it = insertRandomInstructions(MAX_LINES - 1).iterator();
				write(it);
				// invalid: put with 2 arguments.
				// invalidString = generateInvalidInstructions(MAX_INSTRUCTION_LENGTH - 3 - 2, 1, "put");
				invalidString = "put " + generateRandomString(200) + " " + generateRandomString(200);
				pw.println(invalidString);
				break;
			case 20:
				// URL starts with http://

				shuffleContainer.addAll(insertHttpInstructions("http://"));

				Collections.shuffle(shuffleContainer);
				it = shuffleContainer.iterator();
				write(it);

				// invalid: put with 1 argument.
				invalidString = generateInvalidInstructions(MAX_INSTRUCTION_LENGTH - 3 - 1, 0, "put");
				pw.println(invalidString);
				break;
			case 21:
				// URL starts with https://
				shuffleContainer.addAll(insertHttpInstructions("https://"));

				Collections.shuffle(shuffleContainer);
				it = shuffleContainer.iterator();
				write(it);
				// invalid: put with 4(or more) arguments
				numOfArg = generateRandomInt(4, 8);
				invalidString = generateInvalidInstructions(MAX_INSTRUCTION_LENGTH - 3 - numOfArg, numOfArg - 1, "put");
				pw.println(invalidString);
				break;
			case 22:
				// invalid random instructions that are not valid. i.e, ['abc'] instead of
				// ['put'].
				// Length of the random instruction is equal to the length of splitting.
        int randomLen = generateRandomInt(9,11);
        System.out.println("Case 22 Random Len is: "+randomLen);
				String invalidInstruction = generateRandomString(randomLen);
				String invalidArgs = generateInvalidInstructions(MAX_INSTRUCTION_LENGTH - randomLen, randomLen - 1,
						invalidInstruction);
				pw.println(invalidArgs);
				break;
			default:
				break;
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

	private static String generateInvalidInstructions(int seed, int intervals, String instruction) {
		String randomString = generateRandomString(seed);
    String output = instruction;
    int splitUpper = randomString.length();
    switch(intervals){
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
      case 8:
        output = output + " " + randomSplit(randomString, intervals);
        break;
      default:
        output = output + " " + splitMultipleStrings(randomString, splitUpper);
        break;

    }
		return output;
  }
  
  private static String splitMultipleStrings(String input, int upperLimit){
    String temp = "", output ="";
    // For now hardcode. 
    int splitNumber = generateRandomInt(4,upperLimit), tempSplit = 0, splitIndex = generateRandomInt(splitNumber, upperLimit);
    boolean isFirstSplit = true;
    System.out.println("Split number is: "+splitNumber);
    for (int i=0; i < splitNumber-1; i++){
      if(isFirstSplit){
        temp = input.substring(0,splitIndex);
        output += temp;
        isFirstSplit = false;
      }
      if((splitIndex == upperLimit-1)|| (splitIndex == upperLimit)){
        return output;
      }
      tempSplit = splitIndex;
      splitIndex = generateRandomInt(tempSplit, upperLimit);
      output += " "+input.substring(tempSplit, splitIndex);
      System.out.println("Debugger: "+i+"  "+ tempSplit+"  "+splitIndex+"  "+output);
    }
    return output;
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
	// public static String generateRandomStr(int seed) {
	// return Character.toString((char) seed);
	// }

	/**
	 * Produce minimum and maximum length get/rem/put instructions with random
	 * string
	 * 
	 * @return list of the 9 inputs
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
		String save1 = "save " + generateRandomString(SAVE_MAX_INPUT) + " " +
		generateRandomString(MIN_INPUT);
		String save2 = "save " + generateRandomString(MIN_INPUT) + " " +
		generateRandomString(SAVE_MAX_INPUT);

		String[] instructions = { getMin, getMax, remMin, remMax, put1, put2, put3, save1, save2 };

		return new ArrayList<String>(Arrays.asList(instructions));
	}

	private static ArrayList<String> insertNumbersInstructions() {

		String getMin = "get " + generateRandomNumberString(MIN_INPUT);
		String getMax = "get " + generateRandomNumberString(GET_REM_MAX_INPUT);
		String remMin = "rem " + generateRandomNumberString(MIN_INPUT);
		String remMax = "rem " + generateRandomNumberString(GET_REM_MAX_INPUT);
		String put1 = "put " + generateRandomNumberString(MIN_INPUT) + " " + generateRandomNumberString(MIN_INPUT) + " "
				+ generateRandomNumberString(PUT_MAX_INPUT);
		String put2 = "put " + generateRandomNumberString(MIN_INPUT) + " " + generateRandomNumberString(PUT_MAX_INPUT) + " "
				+ generateRandomNumberString(MIN_INPUT);
		String put3 = "put " + generateRandomNumberString(PUT_MAX_INPUT) + " " + generateRandomNumberString(MIN_INPUT) + " "
				+ generateRandomNumberString(MIN_INPUT);
		String save1 = "save " + generateRandomNumberString(SAVE_MAX_INPUT) + " " +
		generateRandomNumberString(MIN_INPUT);
		String save2 = "save " + generateRandomNumberString(MIN_INPUT) + " " +
		generateRandomNumberString(SAVE_MAX_INPUT);

		String[] instructions = { getMin, getMax, remMin, remMax, put1, put2, put3, save1, save2 };

		return new ArrayList<String>(Arrays.asList(instructions));
	}

	private static ArrayList<String> insertAlphabetInstructions() {

		String getMin = "get " + generateRandomAlphabetString(MIN_INPUT);
		String getMax = "get " + generateRandomAlphabetString(GET_REM_MAX_INPUT);
		String remMin = "rem " + generateRandomAlphabetString(MIN_INPUT);
		String remMax = "rem " + generateRandomAlphabetString(GET_REM_MAX_INPUT);
		String put1 = "put " + generateRandomAlphabetString(MIN_INPUT) + " " + generateRandomAlphabetString(MIN_INPUT) + " "
				+ generateRandomAlphabetString(PUT_MAX_INPUT);
		String put2 = "put " + generateRandomAlphabetString(MIN_INPUT) + " " + generateRandomAlphabetString(PUT_MAX_INPUT) + " "
				+ generateRandomAlphabetString(MIN_INPUT);
		String put3 = "put " + generateRandomAlphabetString(PUT_MAX_INPUT) + " " + generateRandomAlphabetString(MIN_INPUT) + " "
				+ generateRandomAlphabetString(MIN_INPUT);
		String save1 = "save " + generateRandomAlphabetString(SAVE_MAX_INPUT) + " " +
		generateRandomAlphabetString(MIN_INPUT);
		String save2 = "save " + generateRandomAlphabetString(MIN_INPUT) + " " +
		generateRandomAlphabetString(SAVE_MAX_INPUT);

		String[] instructions = { getMin, getMax, remMin, remMax, put1, put2, put3, save1, save2 };

		return new ArrayList<String>(Arrays.asList(instructions));
	}

	private static ArrayList<String> insertHttpInstructions(String urlPrefix) {

		int len = urlPrefix.length();

		String getMin = "get " + urlPrefix + generateRandomString(MIN_INPUT);
		String getMax = "get " + urlPrefix + generateRandomString(GET_REM_MAX_INPUT - len);
		String remMin = "rem " + urlPrefix + generateRandomString(MIN_INPUT);
		String remMax = "rem " + urlPrefix + generateRandomString(GET_REM_MAX_INPUT - len);
		String put1 = "put " + urlPrefix + generateRandomString(MIN_INPUT) + " " + generateRandomString(MIN_INPUT) + " "
				+ generateRandomString(PUT_MAX_INPUT - len);
		String put2 = "put " + urlPrefix + generateRandomString(MIN_INPUT) + " " + generateRandomString(PUT_MAX_INPUT - len) + " "
				+ generateRandomString(MIN_INPUT);
		String put3 = "put " + urlPrefix + generateRandomString(PUT_MAX_INPUT - len) + " " + generateRandomString(MIN_INPUT) + " "
				+ generateRandomString(MIN_INPUT);

		String[] instructions = { getMin, getMax, remMin, remMax, put1, put2, put3 };

		return new ArrayList<String>(Arrays.asList(instructions));
	}

	/**
	 * Insert additional seed times same prefix inputs
	 * 
	 * @param prefix specify which instruction (put, save, get, rem)
	 * @param seed   how many times to insert. seed=1 means there are 2 same puts in
	 *               total.
	 * @return
	 */
	private static ArrayList<String> insertSameInstructions(String prefix, int seed) {

		int count = 0;
		ArrayList<String> list = new ArrayList<String>();
		String ins = generateInstructions(prefix, 1).get(0);

		// System.out.println("Same: " + ins);

		list.add(ins);
		while (count < seed) {
			list.add(ins);
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
	 * Randomly produce get/rem/put/save instructions with random string length in
	 * seed times
	 * 
	 * @param seed how many instructions to produce
	 * @return list of No.seed inputs
	 */
	public static ArrayList<String> insertRandomInstructions(int seed) {

		int count = 0;
		Instruction ins;
		ArrayList<String> list = new ArrayList<String>();

		while (count < seed) {
			String output = "";
			ins = generateRamdomInstruction();

			switch (ins) {
			case GET:
				output = generateInstructions("get", 1).get(0);
				list.add(output);
				break;
			case REM:
				output = generateInstructions("rem", 1).get(0);
				list.add(output);
				break;
			case PUT:
				output = generateInstructions("put", 1).get(0);
				list.add(output);
				break;
			default:
				list.add("put a b c"); // should never happen
			}
			count++;
		}

		return list;
	}

	/**
	 * Randomly produce an invalid long input
	 * 
	 * @return
	 */
	private static String insertLongInstructions() {

		String get = "get " + generateRandomString(MAX_LINES * 2);
		String rem = "rem " + generateRandomString(MAX_LINES * 2);
		String put1 = "put " + generateRandomString(MAX_LINES * 2) + " " + generateRandomString(MIN_INPUT) + " "
				+ generateRandomString(PUT_MAX_INPUT);
		String put2 = "put " + generateRandomString(MIN_INPUT) + " " + generateRandomString(MAX_LINES * 2) + " "
				+ generateRandomString(MIN_INPUT);
		String put3 = "put " + generateRandomString(MAX_LINES * 2) + " " + generateRandomString(MIN_INPUT) + " "
				+ generateRandomString(MIN_INPUT);
		// String save1 = "save " + generateRandomString(MAX_LINES * 2) + " " +
		// generateRandomString(MIN_INPUT);
		// String save2 = "save " + generateRandomString(MIN_INPUT) + " " +
		// generateRandomString(MAX_LINES * 2);
		// String masterpw = "masterpw " + generateRandomString(MAX_LINES * 2);

		String[] instructions = { get, rem, put1, put2, put3 };

		return instructions[generateRandomPosition(instructions.length)];
	}

	/**
	 * Randomly produce a specific instruction in seed times
	 * 
	 * @param prefix specify which instruction to produce
	 * @param seed   produce how many instructions
	 * @return list of string
	 */
	private static ArrayList<String> generateInstructions(String prefix, int seed) {

		int count = 0, randomLen = 0, left = 0, right = 0, intervals = 0;
		String randomString = "";
		String output = "";
		ArrayList<String> list = new ArrayList<String>();

		switch (prefix) {
		case "put":
			left = 3;
			right = GET_REM_MAX_INPUT - 2 * WHITE_SPACE;
			intervals = 2;
			break;
		// case "save":
		// left = 2;
		// right = GET_REM_MAX_INPUT - 1 - 1 * WHITE_SPACE;
		// intervals = 1;
		// break;
		case "get":
			left = 1;
			right = GET_REM_MAX_INPUT;
			intervals = 0;
			break;
		case "rem":
			left = 1;
			right = GET_REM_MAX_INPUT;
			intervals = 0;
			break;
		default:
			break;
		}

		while (count < seed) {
			randomLen = generateRandomInt(left, right);
			randomString = generateRandomString(randomLen);
			output = prefix + " " + randomSplit(randomString, intervals);

			list.add(output);
			count++;
		}

		return list;
	}

	/**
	 * Produce seed times list instruction
	 * 
	 * @param seed
	 * @return
	 */
	private static ArrayList<String> insertSaves(int seed) {

		int count = 0, randomLen = 0;
		int left = 2;
		int right = GET_REM_MAX_INPUT - 1 - 1 * WHITE_SPACE;
		int intervals = 1;
		String randomString = "";
		String output = "";
		ArrayList<String> list = new ArrayList<String>();

		while (count < seed) {
			randomLen = generateRandomInt(left, right);
			randomString = generateRandomString(randomLen);
			output = "save " + randomSplit(randomString, intervals);

			list.add(output);
			count++;
		}

		return list;
	}

	private static ArrayList<String> insertLists(int seed) {
		int count = 0;
		ArrayList<String> list = new ArrayList<String>();

		while (count < seed) {
			list.add("list");
			count++;
		}

		return list;

	}

	/**
	 * Generate random position of a string
	 * 
	 * @param len length of the string
	 * @return random index position of the string, should be in [0, len)
	 */
	private static int generateRandomPosition(int len) {

		Random random = new Random();

		int position = random.nextInt(len); // produce [0, len) integer

		return position;
	}

	/**
	 * Random split the long string into substrings
	 * 
	 * @param str the long string
	 * @return
	 */
	private static String randomSplit(String str, int intervals) {

		String output = "";
		int strLen = str.length();

		switch (intervals) {
		case 1:
			if (strLen == 2) {
				output = str.substring(0, 1) + " " + str.substring(1);
			} else {
				int position1 = generateRandomPosition(strLen); // make sure position1 is not at last index

				while (position1 == 0)
					position1 = generateRandomPosition(strLen); // make sure position1 is not 0

				output = str.substring(0, position1) + " " + str.substring(position1);
			}
			break;
		case 2:
			if (strLen == 3) {
				output = str.substring(0, 1) + " " + str.substring(1, 2) + " " + str.substring(2);
			} else {
				int position1 = generateRandomPosition(strLen - 1); // make sure position1 is not at last index

				while (position1 == 0)
					position1 = generateRandomPosition(strLen - 1); // make sure position1 is not 0

				int[] arr = new int[strLen - position1 - 1];

				for (int i = 0; i < strLen - position1 - 1; i++)
					arr[i] = i + position1 + 1; // store what positions left in an array for random

				int j = generateRandomPosition(arr.length); // random the second position to split
				int position2 = arr[j];

				while (position2 == position1) { // make sure position2 is not equal to position1
					j = generateRandomPosition(arr.length);
					position2 = arr[j];
				}
				output = str.substring(0, position1) + " " + str.substring(position1, position2) + " "
						+ str.substring(position2);
			}
			break;
		default:
			output = str; // don't split
			break;
		}

		return output;
	}

	private static Instruction generateRamdomInstruction() {

		int[] insArr = { 0, 1, 2 }; // remove save-3, list-4 and MASTERPW-5

		int index = generateRandomPosition(insArr.length);

		Instruction inst = INSTRUCTIONS[insArr[index]];

		return inst;
	}

	/**
	 * Generate random string in length of len
	 * 
	 * @param len specify the length of produced random string
	 * @return string
	 */
	private static String generateRandomString(int len) {

		int leftLimit = 33; // letter '!'
		int rightLimit = 126; // letter '~'
		if (len == 0) {
			return "";
		}
		Random random = new Random();
		StringBuilder buffer = new StringBuilder(len);

		for (int i = 0; i < len; i++) {
			int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}

		String generatedString = buffer.toString();

		return generatedString;
	}

	private static String generateRandomNumberString(int len) {

		String characters = "0123456789"; 
		StringBuilder buffer = new StringBuilder();
		Random random = new Random();
		
        for (int i = 0; i < len; i++) {
            // nextInt(10) = [0, 10)
            buffer.append(characters.charAt(random.nextInt(10)));
        }
        return buffer.toString();
	}

	private static String generateRandomAlphabetString(int len) {

		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"; 
		StringBuilder buffer = new StringBuilder();
		Random random = new Random();
		
        for (int i = 0; i < len; i++) {
            // nextInt(10) = [0, 10)
            buffer.append(characters.charAt(random.nextInt(52)));
        }
        return buffer.toString();
	}

	/**
	 * Get the URL (second argument) of an instruction
	 * 
	 * @param ins any instructions: put a b c
	 * @return a
	 */
	private static String getURL(String ins) {
		return ins.split(" ")[1];
	}

	/**
	 * Get the master password (second argument) of SAVE
	 * 
	 * @param ins save instructions: save a b
	 * @return a
	 */
	private static String getMasterPw(String ins) {
		return ins.split(" ")[1];
	}

	/**
	 * Read current state value from the properties
	 * 
	 * @param filePath file path
	 * @param key
	 * @return current state value
	 */
	private static int getValue(String filePath, String key) {
		Properties p = new Properties();

		try {
			InputStream in = new BufferedInputStream(new FileInputStream(filePath));
			p.load(in);
			String value = p.getProperty(key);

			return Integer.parseInt(value);
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	private static void writeProperty(String filePath, String key, int value) {
		Properties p = new Properties();

		try {
			OutputStream out = new FileOutputStream(filePath);
			p.setProperty(key, Integer.toString(value));
			p.store(out, "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void write(Iterator<String> it) {
		while (it.hasNext())
			pw.println(it.next());
	}

}
