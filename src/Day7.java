import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Day7 {
  public static void main(String[] args) throws IOException {
    long splitCount = countSplits("./src/Day7Input.txt");

    System.out.println("The Splits in Part 1 are: " + splitCount);

    long timelineCount = countTimelines("./src/Day7Input.txt");
    System.out.println("Part 2: " + timelineCount);
  }

  public static long countSplits(String filename) throws IOException {
    List<String> lines = Files.readAllLines(Path.of(filename));
    int rows = lines.size();
    int cols = lines.get(0).length();

    // Find starting S index
    int startCol = -1;
    for (int c = 0; c < cols; c++) {
      if (lines.get(0).charAt(c) == 'S') {
        startCol = c;
        break;
      }
    }
    // Track which columns have active beams in a given row
    // Use a Set to handle the overlap of beams

    Set<Integer> activeBeams = new HashSet<>();
    activeBeams.add(startCol);

    long splitCount = 0;

    // row by row, skip S row
    for (int row = 1; row < rows; row++) {
      String line = lines.get(row);
      Set<Integer> newBeams = new HashSet<>();

      for (int col: activeBeams) {
        char cell = line.charAt(col);

        if (cell == '^') {
          // Beam hits a splitter, count it, and propagate
          splitCount++;
          if (col - 1 >= 0) newBeams.add(col - 1);
          if (col + 1 < cols) newBeams.add(col + 1);
        } else {
          // Beam continues downward.
          newBeams.add(col);
        }
      }
      activeBeams = newBeams;
      if (activeBeams.isEmpty()) break;
    }

    return splitCount;
  }

  public static long countTimelines(String filename) throws IOException {
    List<String> lines = Files.readAllLines(Path.of(filename));
    int rows = lines.size();
    int cols = lines.get(0).length();

    int startCol = -1;
    for (int c = 0; c < cols; c++) {
      if (lines.get(0).charAt(c) == 'S') {
        startCol = c;
        break;
      }
    }

    Map<Integer, Long> timelines = new HashMap<>();
    timelines.put(startCol, 1L);

    for (int row = 1; row < rows; row++) {
      String line = lines.get(row);
      Map<Integer, Long> newTimelines = new HashMap<>();

      for (Map.Entry<Integer, Long> entry: timelines.entrySet()) {
        int col = entry.getKey();
        long count = entry.getValue();
        char cell = line.charAt(col);
        if (cell == '^') {
          if (col - 1 >= 0) newTimelines.merge(col - 1, count, Long::sum);
          if (col + 1 < cols) newTimelines.merge(col + 1, count, Long::sum);
        } else {
          newTimelines.merge(col, count, Long::sum);
        }
      }
      timelines = newTimelines;
      if (timelines.isEmpty()) break;
    }

    return timelines.values().stream().mapToLong(Long::longValue).sum();
  }
}
