import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Day5 {
  public static void main(String[] args) throws IOException {
    Map<String, List<String>> mappedStrings = readInput("./src/Day5Input.txt");
    List<Range> ranges = mappedStrings.get("ranges")
                                      .stream()
                                      .map(Range::fromString)
                                      .toList();
    List<Long> candidates = mappedStrings.get("candidates").stream().map(Long::parseLong).toList();
    long freshCount = candidates.stream()
                                .filter(id -> ranges.stream()
                                                          .anyMatch(r -> r.contains(id)))
                                                          .count();

    System.out.println("Part 1 Fresh Ingredient Count: " + freshCount);

    List<Range> mergedRanges = mergeRanges(ranges);
    long freshIngredientIdCount = mergedRanges.stream().mapToLong(Range::size).sum();
    System.out.println("Part 2 Fresh Ingredient Ids Count: " + freshIngredientIdCount);
  }

  record Range(long start, long end) {
    boolean contains(long id) {
      return start <= id && id <= end;
    }

    static Range fromString(String s) {
      String[] parts = s.split("-");
      return new Range(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
    }

    long size() {
      return end - start + 1;  // Inclusive
    }
  }

  static List<Range> mergeRanges(List<Range> ranges) {
    if (ranges.isEmpty()) return ranges;

    List<Range> sorted = ranges.stream().sorted(Comparator.comparingLong(Range::start)).toList();
    List<Range> mergedRanges = new ArrayList<>();

    Range current = sorted.get(0);

    for (int i = 1; i < sorted.size(); i++) {
      Range next = sorted.get(i);
      if (next.start() <= current.end() + 1) {
        current = new Range(current.start(), Math.max(current.end(), next.end()));
      } else {
        mergedRanges.add(current);
        current = next;
      }
    }
    mergedRanges.add(current);
    return mergedRanges;
  }

  public static Map<String, List<String>> readInput(String filename)  throws IOException {
    Map<String, List<String>> mappedStrings = new HashMap<>();
    List<String> ranges = new ArrayList<>();
    List<String> candidates = new ArrayList<>();
    boolean inCandidates = false;

    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.isEmpty()) {
          inCandidates = true;
          continue;
        }

        if (inCandidates) {
          candidates.add(line);
        } else ranges.add(line);
      }
    }
    mappedStrings.put("ranges", ranges);
    mappedStrings.put("candidates", candidates);
    return mappedStrings;
  }
}
