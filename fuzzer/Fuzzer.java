import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/* a stub for your team's fuzzer */
public class Fuzzer {

	private static final String OUTPUT_FILE = "fuzz.txt";
	private static int commandNum = 0;
	private static final String STATUS_FILE = "status.txt";

	private final static int MAX_LINES = 1024;
	private final static int MAX_INSTRUCTION_LENGTH = 1022;
	private final static int PUT_MAX_INPUT = MAX_INSTRUCTION_LENGTH - 6 - 1 - 1;
	private final static int GET_REM_MAX_INPUT = MAX_INSTRUCTION_LENGTH - 4;
	private static Instruction[] INSTRUCTIONS = Instruction.values();

	private final static int PUT = 0;
	private final static int GET = 1;
	private final static int REM = 2;
	private final static int SAVE = 3;
	private final static int LIST = 4;
	private final static int MASTERPW = 5;

	public static void main(String[] args) throws IOException {
		System.out.println(Instruction.getBNF());
		
		

		System.out.println("random string: " + generateRandomString(20));

		FileOutputStream out = null;
		PrintWriter pw = null;

		Integer runtime;
		try {
			out = new FileOutputStream(OUTPUT_FILE);
			pw = new PrintWriter(out);

			// TODO: 这两行将来放到loop里，生成随机char。
			int seed = generateRandomInt(32, 126);
			String ascii = generateRandomStr(seed);
			System.out.println("random char: " + ascii);

//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(STATUS_FILE), "utf-8"));
//            int status = Integer.parseInt(bufferedReader.readLine());

			/*
			 * We just print one instruction. Hint: you might want to make use of the
			 * instruction grammar which is effectively encoded in Instruction.java
			 */

			boolean notListCommand = true;

			int numberLines = 0, tempInsSeed = 0, numberStrings = 0;
			int strLen = 1;

			while (numberLines < MAX_LINES - 1) {

				String output = "";
				Instruction ins = generateRamdomInstruction();
				Random random = new Random();

				/* strLen should be in [1, 1014] for PUT, in [1, 1018] for GET and REM */
				switch (ins) {

				case PUT:
					String temp = "";

					if (strLen > PUT_MAX_INPUT)
						strLen = random.nextInt(PUT_MAX_INPUT + 1);

					if (strLen == 1) {
						temp = generateRandomString(strLen + 2);
						output = ins.getOpcode() + " " + temp.substring(0, 1) + " " + temp.substring(1, 2) + " "
								+ temp.substring(2);
					} else if (strLen == 2) {
						temp = generateRandomString(strLen + 1);
						output = ins.getOpcode() + " " + temp.substring(0, 1) + " " + temp.substring(1, 2) + " "
								+ temp.substring(2);
					} else if (strLen == 3) {
						temp = generateRandomString(strLen);
						output = ins.getOpcode() + " " + temp.substring(0, 1) + " " + temp.substring(1, 2) + " "
								+ temp.substring(2);
					} else {
						temp = generateRandomString(strLen);
						int len = temp.length();
						int position1 = random.nextInt(len - 2); // random the first position to split: [0, len - 2)
						
						while (position1 == 0) position1 = random.nextInt(len - 2);  // make sure position1 is not 0
						
//						System.out.println("===============len: "+len);
//						System.out.println("===============posotion1: "+position1);
						int[] arr = new int[len - position1 - 1];

						for (int i = 0; i < len - position1 - 1; i++) {				
							arr[i] = i + position1; // store what position left in an array for random	
						}

						int j = random.nextInt(arr.length);  // random the second position to split
						int position2 = arr[j];
						
						while (position2 == position1) {
							j = random.nextInt(arr.length);
							position2 = arr[j];
						}
						
//						System.out.println("===============posotion2: "+position2);

						output = ins.getOpcode() + " " + temp.substring(0, position1) + " "
								+ temp.substring(position1, position2) + " " + temp.substring(position2);
//						System.out.println("===============output: "+output);
					}

					System.out.println("PUT: " + strLen);
//					output = ins.getOpcode() + " " + generateRandomString(strLen) + " " + generateRandomString(strLen)
//							+ " " + generateRandomString(strLen);
					System.out.println("PUT: " + output.length());
					pw.println(output);
//					System.out.println(output);

					break;
				case GET:
					if (strLen > GET_REM_MAX_INPUT)
						strLen = random.nextInt(GET_REM_MAX_INPUT + 1);

					System.out.println("GET:" + strLen);
					output = ins.getOpcode() + " " + generateRandomString(strLen);
					System.out.println("GET: " + output.length());
					pw.println(output);
//					System.out.println(output);

					break;
				case REM:
					if (strLen > GET_REM_MAX_INPUT)
						strLen = random.nextInt(GET_REM_MAX_INPUT + 1);

					System.out.println("REM: " + strLen);
					output = ins.getOpcode() + " " + generateRandomString(strLen);
					System.out.println("REM: " + output.length());
					pw.println(output);
//					System.out.println(output);
					break;
//				case LIST:
//					output = ins.getOpcode();
//					pw.println(output);
//					System.out.println(output);
//					break;
				}

//				switch (tempInsSeed) {
//				case PUT:
//					numberStrings = 3;
//					break;
//				case GET:
//					numberStrings = 1;
//					break;
//				case REM:
//					numberStrings = 1;
//					break;
////				case SAVE:
////					numberStrings = 2;
////					break;
//				case LIST:
//					numberStrings = 0;
//					break;
////				case MASTERPW:
////					numberStrings = 1;
////					break;
//				}

				numberLines++;
				strLen++;
			}

			// "list" in the end
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
	// Add some randominisation.

	public static int generateRandomInt(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	public static String generateRandomStr(int seed) {
		return Character.toString((char) seed);
	}

	private static Instruction generateRamdomInstruction() {

		int[] insArr = { 0, 1, 2 }; // remove SAVE-3 and MASTERPW-5

		Random random = new Random();

		int index = random.nextInt(insArr.length); // produce [0,4) integer

		Instruction inst = INSTRUCTIONS[insArr[index]];

		return inst;
	}

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

//	private static String

}
