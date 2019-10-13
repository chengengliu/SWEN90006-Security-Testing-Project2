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
import java.util.ArrayList;
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
//	private static final String STATUS_FILE = "status.txt";
	private static final String PROPERTIES = "state.properties";

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
			case SAVE:
				output = generateInstructions("save", 1).get(0);
				list.add(output);
				break;
			default:
				list.add("list"); // should never happen
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

		String[] instructions = { get, rem, put1, put2, put3 };

		return instructions[generateRandomPosition(instructions.length)];
	}
	
	/**
	 * Randomly produce a specific instruction in seed times
	 * @param prefix specify which instruction to produce
	 * @param seed produce how many instructions
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
		case "save":
			left = 2;
			right = GET_REM_MAX_INPUT - 1 - 1 * WHITE_SPACE;
			intervals = 1;
			break;
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
	 * Random split the long string into three substrings
	 * 
	 * @param str the long string, which length is in [1, 1016]
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
			output = str;  // don't split
			break;
		}

		return output;
	}

	private static Instruction generateRamdomInstruction() {

		int[] insArr = { 0, 1, 2, 3 }; // remove SAVE-3, list-4 and MASTERPW-5

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

		Random random = new Random();
		StringBuilder buffer = new StringBuilder(len);

		for (int i = 0; i < len; i++) {
			int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}

		String generatedString = buffer.toString();

		return generatedString;
	}

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

      return output;
    }

    private static String[] getMoreStrNum(int inst){
      

      return null;
    }
}