import java.io.*;
import java.util.*;

public class Day01 {
  public static void main(String[] args) throws IOException {
    List<String> rotations = readInput("./src/rotations.txt");
    int password = solve(rotations);
    System.out.println("Password: " + password);
  }

  public static int solve(List<String> rotations) {
    int position = 50;
    int zeroCount = 0;

    for (String rotation : rotations) {
      char direction =  rotation.charAt(0);
      int distance = Integer.parseInt(rotation.substring(1));

      if (direction == 'L') {
        position = (position - distance) % 100;
        // negative modulo gets a little weird
        if (position < 0) {
          position += 100;
        }
      } else { //The "R" case
        position = (position + distance) % 100;
      }
      if (position == 0) {
        zeroCount++;
      }
    }
    return zeroCount;
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