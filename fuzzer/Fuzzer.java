import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/* a stub for your team's fuzzer */
public class Fuzzer {

	private static final String OUTPUT_FILE = "fuzz.txt";
	private static final String STATUS_FILE = "status.txt";

	private static final int RANDOM_SEED = 10;
	private final static int MAX_LINES = 1024;
	private final static int MAX_INSTRUCTION_LENGTH = 1022;
	private final static int INS_LENGTH = 3;
	private final static int WHITE_SPACE = 1;
	private final static int MIN_INPUT = 1;
	private final static int PUT_MAX_INPUT = MAX_INSTRUCTION_LENGTH - INS_LENGTH - WHITE_SPACE * 3 - 1 - 1;
	private final static int GET_REM_MAX_INPUT = MAX_INSTRUCTION_LENGTH - INS_LENGTH - WHITE_SPACE;

	private static FileOutputStream out = null;
	private static PrintWriter pw = null;
	private static Instruction[] INSTRUCTIONS = Instruction.values();

	public static void main(String[] args) throws IOException {

		ArrayList<String> shuffleContainer = new ArrayList<String>();

		System.out.println(Instruction.getBNF());

		Iterator<String> it = null;

		try {
			out = new FileOutputStream(OUTPUT_FILE);
			pw = new PrintWriter(out);

			it = insertOrderedInstructions(1).iterator();
			write(it);

			ArrayList<String> minMaxList = insertMinMaxInstructions();
			ArrayList<String> randomList = insertRandomInstructions(RANDOM_SEED);

			shuffleContainer.addAll(minMaxList);
			shuffleContainer.addAll(randomList);
			shuffleContainer.addAll(insertSamePut(1));
			shuffleContainer.addAll(generateRandomPut(500));
			shuffleContainer.addAll(generateRandomGetOrRem("get", 20));

			Collections.shuffle(shuffleContainer); // randomize these inputs

			it = shuffleContainer.iterator();
			write(it);

			/* "list" in the end */
			pw.println("list");

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
	 * @param seed how many times to insert. seed=1 means there are 2 same puts in total.
	 * @return
	 */
	private static ArrayList<String> insertSamePut(int seed) {

		int count = 0;
		ArrayList<String> list = new ArrayList<String>();
		String put = generateRandomPut();
		
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
			list.add(generateRandomGetOrRem("get"));
			list.add(generateRandomGetOrRem("rem"));
			list.add(generateRandomPut());

			count++;
		}
		count = 0;
		while (count < seed) {
			// get -> put -> rem
			list.add(generateRandomGetOrRem("get"));
			list.add(generateRandomPut());
			list.add(generateRandomGetOrRem("rem"));

			count++;
		}
		count = 0;
		while (count < seed) {
			// rem -> get -> put
			list.add(generateRandomGetOrRem("rem"));
			list.add(generateRandomGetOrRem("get"));
			list.add(generateRandomPut());

			count++;
		}
		count = 0;
		while (count < seed) {
			// rem -> put -> get
			list.add(generateRandomGetOrRem("rem"));
			list.add(generateRandomPut());
			list.add(generateRandomGetOrRem("get"));

			count++;
		}
		count = 0;
		while (count < seed) {
			// put -> rem -> get
			list.add(generateRandomPut());
			list.add(generateRandomGetOrRem("rem"));
			list.add(generateRandomGetOrRem("get"));

			count++;
		}
		count = 0;
		while (count < seed) {
			// put -> get -> rem
			list.add(generateRandomPut());
			list.add(generateRandomGetOrRem("get"));
			list.add(generateRandomGetOrRem("rem"));

			count++;
		}

		return list;
	}

	/**
	 * 
	 * Randomly produce get/rem/put instructions with random string length
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
				output = generateRandomGetOrRem("get");
				list.add(output);
				break;
			case REM:
				output = generateRandomGetOrRem("rem");
				list.add(output);
				break;
			case PUT:
				output = generateRandomPut();
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
	 * Randomly produce a valid PUT input
	 * 
	 * @return put input
	 */
	private static String generateRandomPut() {

		int randomLen = generateRandomInt(1, GET_REM_MAX_INPUT - 2 * WHITE_SPACE); // 1018 - 2*1
		String randomString = generateRandomString(randomLen);
		String output = "put " + randomSplit(randomString);

		return output;
	}
	
	/**
	 * Randomly produce seed times valid PUT input
	 * @param seed 
	 * @return list
	 */
	private static ArrayList<String> generateRandomPut(int seed) {
		
		int count = 0;
		ArrayList<String> list = new ArrayList<String>();

		while (count < seed) {
			list.add(generateRandomPut());
			count++;
		}

		return list;
	}

	/**
	 * Randomly produce a valid GET or REM input
	 * 
	 * @param prefix
	 * @return get or rem input
	 */
	private static String generateRandomGetOrRem(String prefix) {

		int randomLen = generateRandomInt(1, GET_REM_MAX_INPUT);
		String output = prefix + " " + generateRandomString(randomLen);

		return output;
	}
	
	/**
	 * Randomly produce seed times valid GET or REM input
	 * 
	 * @param prefix
	 * @return list of get or rem input
	 */
	private static ArrayList<String> generateRandomGetOrRem(String prefix, int seed) {

		int count = 0;
		ArrayList<String> list = new ArrayList<String>();

		while (count < seed) {
			list.add(generateRandomGetOrRem(prefix));
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
	private static String randomSplit(String str) {

		String output = "";
		int strLen = str.length();

		// make sure that str can be split into three substrings
		if (strLen == 1) {
			String temp = generateRandomString(strLen + 2);
			output = temp.substring(0, 1) + " " + temp.substring(1, 2) + " " + temp.substring(2);
		} else if (strLen == 2) {
			String temp = generateRandomString(strLen + 1);
			output = temp.substring(0, 1) + " " + temp.substring(1, 2) + " " + temp.substring(2);
		} else if (strLen == 3) {
			output = str.substring(0, 1) + " " + str.substring(1, 2) + " " + str.substring(2);
		} else {
			int position1 = generateRandomPosition(strLen - 2); // make sure position1 is not at last two index

			while (position1 == 0)
				position1 = generateRandomPosition(strLen - 2); // make sure position1 is not 0

			int[] arr = new int[strLen - position1 - 1];

			for (int i = 0; i < strLen - position1 - 1; i++)
				arr[i] = i + position1; // store what positions left in an array for random

			int j = generateRandomPosition(arr.length); // random the second position to split
			int position2 = arr[j];

			while (position2 == position1) { // make sure position2 is not equal to position1
				j = generateRandomPosition(arr.length);
				position2 = arr[j];
			}
			output = str.substring(0, position1) + " " + str.substring(position1, position2) + " "
					+ str.substring(position2);
		}

		return output;
	}

	private static Instruction generateRamdomInstruction() {

		int[] insArr = { 0, 1, 2 }; // remove SAVE-3, list-4 and MASTERPW-5

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

	private static void write(Iterator<String> it) {
		while (it.hasNext())
			pw.println(it.next());
	}

}