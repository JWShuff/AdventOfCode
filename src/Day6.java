import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class Day6 {
  public static void main(String[] args) throws IOException {
    long part1Result = squidMathSum("./src/Day6Input.txt");
    System.out.println("Part 1 Mindflayer Math Sum " + part1Result);

    long part2Result = rightToLeftMath("./src/Day6Input.txt");
    System.out.println("Part 2 Mindflayer Math Sum " + part2Result);
  }

  public static long squidMathSum(String filename) throws IOException {
    List<String> lines = Files.readAllLines(Path.of(filename));

    String[] ops = lines.get(4).trim().split("\\s+");
//    System.out.println("The Operators are " + ops.length + " nums long");
    String[][] numbers = new String[4][];
    for (int i = 0; i < 4; i++) {
      numbers[i]= lines.get(i).trim().split("\\s+");

    }

//    System.out.println("The Num Arrays are " + numbers.length + " 'wide' and " + numbers[0].length + " 'long'" );
   long grandTotal = 0;
    for (int col = 0; col < ops.length; col++) {
      // Handle the first multiplier operation vs the first add with 'right' starting digit.
      long result = (ops[col].equals("+") ? 0 : 1);
      for (int row = 0; row < 4; row++) {
        long num = Long.parseLong(numbers[row][col]);
        result = ops[col].equals("+") ? result + num : result * num;
      }
      grandTotal += result;
    }
    return grandTotal;
  }

  public static long rightToLeftMath(String filename) throws IOException {
    List<String> lines = Files.readAllLines(Path.of(filename));

    String operatorLine = lines.get(4).toString();
    List<int[]> problemRanges = findColumnRanges(lines);
    long grandTotal = 0;

    for (int p = problemRanges.size() - 1; p >= 0; p--) {
      int start =  problemRanges.get(p)[0];
      int end = problemRanges.get(p)[1];

      // Set operator
      char op = '+';
      for (int i = start; i <= end; i++) {
        char c = operatorLine.charAt(i);
        if ( c == '*' || c == '+') { op = c; break; }
      }

      List<Long> numbers = new ArrayList<>();
      for (int col = end; col >= start; col--) {
        StringBuilder digits = new StringBuilder();
        for (int row = 0; row < 4; row++) {
          String line = lines.get(row);
          if (col < line.length()) {
            char c = line.charAt(col);
            if (c != ' ') digits.append(c);
          }
        }

        if (digits.length() > 0) {
          numbers.add(Long.parseLong(digits.toString()));
        }
      }

      long result = (op == '+') ? 0 : 1;
      for (long n : numbers) {
        result = (op == '+') ? result + n : result * n;
      }
      grandTotal += result;
    }
    return grandTotal;
  }

  private static List<int[]> findColumnRanges(List<String> lines) {
    // Find max line length
    int maxLen = lines.stream().mapToInt(String::length).max().orElse(0);

    List<int[]> ranges = new ArrayList<>();
    int start = -1;

    for (int col = 0; col < maxLen; col++) {
      boolean allSpaces = true;
      // Check only the 4 number lines (not operator line)
      for (int row = 0; row < 4; row++) {
        String line = lines.get(row);
        if (col < line.length() && line.charAt(col) != ' ') {
          allSpaces = false;
          break;
        }
      }

      if (allSpaces) {
        if (start != -1) {
          ranges.add(new int[]{start, col - 1});
          start = -1;
        }
      } else {
        if (start == -1) start = col;
      }
    }

    // Don't forget the last range
    if (start != -1) {
      ranges.add(new int[]{start, maxLen - 1});
    }

    return ranges;
  }
}
