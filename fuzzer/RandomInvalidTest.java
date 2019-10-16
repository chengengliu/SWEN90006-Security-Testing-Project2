import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomInvalidTest {
  private final static int MAX_INSTRUCTION_LENGTH = 1022;
  private static FileOutputStream out = null;
  private static PrintWriter pw = null;
  public static void main(String[] args) throws IOException{

    try{
      int lengthOfArg = MAX_INSTRUCTION_LENGTH/2;

      out = new FileOutputStream("test.txt");
      pw = new PrintWriter(out);
      for (int i =0; i < 1000; i++){
        int numOfArg = generateRandomInt(4, 8);
        String invalidString = generateInvalidInstructions(MAX_INSTRUCTION_LENGTH-3-numOfArg, 9, "put");
        // Boundary check. 
        String output = invalidString.substring(0,MAX_INSTRUCTION_LENGTH);
        pw.println(output);
      }
    }catch(Exception e){
      e.printStackTrace();
      System.exit(1);
    }finally{
      if(pw!=null){
        pw.flush();
      }
      if(out!=null){
        out.close();
      }
    }
  }
    // Insert one space after one char. The number of input is limited to 1022/2. 


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
        case 9:
          output = output + " " + randomSplit(randomString, 1);
        default:
          output = output + " " + splitMultipleStrings(splitUpper);
          break;
  
      }
      return output;
    }
    private static String splitOneSpace(String input){
      String output ="";
      for (int i=0; i < input.length(); i++){
        if( i+1 == input.length()) return output+input.charAt(i);
        output+=input.substring(i,i+1)+" ";
      }
      return output;
    }
    
    private static String splitMultipleStrings(int insLen){
      String output ="";
      output = splitOneSpace(generateRandomString(Math.floorDiv(MAX_INSTRUCTION_LENGTH-1-insLen,2)));
      return output;
    }
  
  
  /**
   * 
   * @param seed  The maximum length of the input. Will be splited into parts. 
   * @param intervals How many part would you like. Number of parts = intervals +1
   * @param instruction
   * @return
   */
  private static int generateRandomPosition(int len) {

		Random random = new Random();

		int position = random.nextInt(len); // produce [0, len) integer

		return position;
  }
  
  private static int generateRandomInt(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
  }
  
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
  
}