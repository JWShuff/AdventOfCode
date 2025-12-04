import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Day4 {
  public static void main(String[] args) throws IOException {
    List<String> lines = readInput("./src/Day4Input.txt");
    int accessibleCount = countAccessible(lines);
    System.out.println("The accessible paper rolls for Part 1 are: " + accessibleCount);
    int removableCount = countTotalRemovable(lines);
    System.out.println("In this Part 2 nightmare warehouse, we can remove " + removableCount + " Paper Rolls");
  }

  public static int countTotalRemovable(List<String> lines) {
    char[][] mutableGrid = new char[lines.size()][];
    for (int i = 0; i < lines.size(); i++) {
      mutableGrid[i] = lines.get(i).toCharArray();
    }

    int totalRemoved = 0;
    // Start with true for the first pass
    boolean foundRemoval = true;

    while (foundRemoval) {
      List<int[]> toRemove = new ArrayList<>();
      int max = mutableGrid[0].length;
      for (int y = 0; y < mutableGrid.length; y++) {
        for (int x = 0; x < mutableGrid[y].length; x++) {
          // Still skip on empty squares
          if (mutableGrid[y][x] != '@') continue;
          int neighborCount = countAround(x, y, max, mutableGrid);
          if (neighborCount < 4) {
            // We might be able to skip this, and just remove as we ID and increment our totalRemoved
            // ...but being cautious
            toRemove.add(new int[]{y, x});
          }
        }
      }

      if (toRemove.isEmpty()) {
        foundRemoval = false;
      } else {
        for (int[] pos: toRemove) {
          mutableGrid[pos[0]][pos[1]] = '.';
          totalRemoved++;
          foundRemoval = true;
        }
      }
    }

    return totalRemoved;
  }

  public static int countAccessible(List<String> lines) {
    int accessibleCount = 0;
    int max = lines.size();
    for (int y = 0; y < max; y++) {
      String line = lines.get(y);
      for (int x = 0; x < line.length(); x++) {
        // Only locations with a 'paper roll'
        if (line.charAt(x) != '@') continue;
        int neighborCount = countAround(x, y, max, lines);

        if (neighborCount < 4) accessibleCount++;
      }
    }

    return accessibleCount;
  }

  public static int countAround(int x, int y, int max, List<String> lines) {
    int neighborCount = 0;
    for (int dx = -1; dx <= 1; dx++) {
      for (int dy = -1; dy <= 1; dy++) {
        // Skip self
        if (dx == 0 && dy == 0) continue;
        int nx = x + dx;
        int ny = y + dy;
        // Out of bounds means it is empty, otherwise check if the ny/nx location is occupied.
        if (inBounds(nx, ny, max) && lines.get(ny).charAt(nx) == '@') {
          neighborCount++;
        }
      }
    }
    return neighborCount;
  }

  // TIL this is legal and common in Java world. Java will pick the method based on the args.
  public static int countAround(int x, int y, int max, char[][] grid) {
    int neighborCount = 0;
    for (int dx = -1; dx <= 1; dx++) {
      for (int dy = -1; dy <= 1; dy++) {
        if (dx == 0 && dy == 0) continue;
        int nx = x + dx;
        int ny = y + dy;
        if (inBounds(nx, ny, max) && grid[ny][nx] == '@') {
          neighborCount++;
        }
      }
    }
    return neighborCount;
  }

  public static boolean inBounds(int x, int y, int max) {
    boolean inBounds;
    if (x < 0 || y < 0 || x >= max || y >= max) {
      inBounds = false;
    } else inBounds = true;

    return inBounds;
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
