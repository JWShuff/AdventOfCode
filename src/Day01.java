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

    ///  Count every time the dials *crosses* 0
    for (String rotation : rotations) {
      char direction = rotation.charAt(0);
      int distance = Integer.parseInt(rotation.substring(1));
//      System.out.println("Rotation: " + rotation + " from " + position + " adds " + countZeroCrossings(position, distance, direction) + " crossings");
      zeroCount += countZeroCrossings(position, distance, direction);
      // Update position for next rotation
      if (direction == 'L') {
        position = (position - distance) % 100;
        if (position < 0) {
          position += 100;
        }
      } else { // 'R'
        position = (position + distance) % 100;
      }
//      System.out.println("Final position: " + position);
    }
    return zeroCount;
  }

  public static int solvePart1(List<String> rotations) {
    int position = 50;
    int zeroCount = 0;

    for (String rotation: rotations) {
      char direction = rotation.charAt(0);
      int distance = Integer.parseInt(rotation.substring(1));
      if (direction == 'L') {
        // Negative modulo is weird in java, this solves it in a one liner rather than an if position < 0 approach
        position = ((position - distance) % 100 + 100) % 100;
      } else {
        position = (position + distance) % 100;
      }

      if (position == 0) {
        zeroCount++;
      }
    }

    return zeroCount;
  }

  public static int countZeroCrossings(int start, int distance, char direction) {
    int crossings = 0;

    if (direction == 'R') {
      int firstZero = 100 - start;
      if (distance >= firstZero) {
        crossings = 1 + (distance - firstZero) / 100;
      }
    } else { // 'L'
      int firstZero = (start == 0) ? 100 : start;
      if (distance >= firstZero) {
        crossings = 1 + (distance - firstZero) / 100;
      }
    }

    return crossings;
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