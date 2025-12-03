import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Day3 {
  public static void main(String[] args) throws IOException {
    String fileName = "./src/Day3Input.txt";
    List<String> lines =  readInput(fileName);
    int totalJoltage = getBasicJoltage(lines);

    System.out.println("The Part 1 Total Joltage is: " + totalJoltage);

    long bestJoltage = getBigJoltage(lines);
    System.out.println("The Part 2 Best Joltage is: " + bestJoltage);
  }

  public static int getBasicJoltage(List<String> lines) {
    int totalJoltage = 0;
    for (String line : lines) {
      int n = line.length();
      int[] digits = new int[n];
      int[] maxRight = new int[n];

      // get my nums in order
      for (int i = 0; i < n; i++) {
        digits[i] = line.charAt(i) - '0';
      }

      int runningMax = -1;
      // now starting from the right of the digits, figure out the highest number to the right of it.
      for (int i = n - 1; i >= 0; i--) {
        maxRight[i] = runningMax;
        runningMax = Math.max(runningMax, digits[i]);
      }
      int bestJoltage = 0;

      // now figure out which of these combos of 10s place digits[i] + maxRight[i] is biggest
      for (int i = 0; i < n - 1; i++) {
        int candidate = digits[i] * 10 + maxRight[i];
        bestJoltage = Math.max(bestJoltage, candidate);
      }
      // that's the #
      totalJoltage += bestJoltage;
    }

    return totalJoltage;
  }

  public static long getBigJoltage(List<String> lines) {
    long totalJoltage = 0;
    for (String line : lines) {
      int n = line.length();
      int k = n - 12; // Number of lines to remove
      Stack<Integer> stack  = new Stack<>();

      for (int i = 0; i < n; i++) {
        int digit =  line.charAt(i) - '0'; // Get the digit using the - '0' trick.

        // Guard against popping off the empty stack, still have k digits to remove, and the next number in the stack is
        // less than our candidate next digit:
        while(!stack.isEmpty() && k > 0 && stack.peek() < digit) {
          stack.pop();
          k--;
        }

        // First digit gets in for free, the rest hit the ln 62 while block.
        stack.push(digit);
      }

      // In case it's already sorted the way we want, need to make sure k is zeroed out.
      while (k > 0 && !stack.isEmpty()) {
        stack.pop();
        k--;
      }

      StringBuilder sb = new StringBuilder();
      for (int digit : stack) { // this just 'pops' bottom to top, so in our expected order
        sb.append(digit);
      }
//      System.out.println("Line: " + line + " | k=" + (n-12) + " | result=" + sb.toString());
      long bestJoltage = Long.parseLong(sb.toString());
      totalJoltage += bestJoltage;
    }

    return totalJoltage;
  }

  public static List<String> readInput(String filename)  throws IOException {
    List<String> lines = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
      String line;
      while ((line = br.readLine()) != null) {
        lines.add(line);
      }
    }
    return lines;
  }
}
