import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;

public class Day11 {
  static long pathsChecked = 0;
  static Map<String, Long> cacheWithBoth = new HashMap<>();
  static Map<String, Long> cacheWithDac = new HashMap<>();
  static Map<String, Long> cacheWithFft = new HashMap<>();
  static Map<String, Long> cacheWithNeither = new HashMap<>();

  public static void main(String[] args) throws IOException {
    String filename = "./src/Day11Input.txt";
    HashMap<String, List<String>> mappedStrings = parseInput(filename);
    System.out.println("The You Key has: " + mappedStrings.get("you"));
    Set<String> visitedPaths = new HashSet<String>();
    Set<String> targetedPaths = Set.of("fft", "dac");
    int depth = 0;
    long preciseAnswer = countCertainPaths("svr", mappedStrings, visitedPaths, false, false);
    System.out.println("The part 2 total outlet are: " + preciseAnswer);
  }

  public static long countPaths(String current, HashMap<String, List<String>> mappedStrings) {
    if (current.equals("out")) {
      return 1;
    }
    List<String> neighbors =  mappedStrings.get(current);
    if (neighbors == null) {
      return 0;
    }

    long totalOutlets = 0;
    for (String neighbor : neighbors) {
      totalOutlets += countPaths(neighbor, mappedStrings);
    }
    return totalOutlets;
  }

  public static long countCertainPaths(String current,
                                       HashMap<String, List<String>> mappedStrings,
                                       Set<String> visitedPaths,
                                       boolean seenDac,
                                       boolean seenFft) {
    if (visitedPaths.contains(current)) {
      return 0;
    }

    // Update seen flags
    seenDac = seenDac || current.equals("dac");
    seenFft = seenFft || current.equals("fft");

    if (current.equals("out")) {
      return (seenDac && seenFft) ? 1 : 0;
    }

    // Check cache
    Map<String, Long> cache = getCache(seenDac, seenFft);
    if (cache.containsKey(current)) {
      return cache.get(current);
    }

    List<String> neighbors = mappedStrings.get(current);
    if (neighbors == null) {
      return 0;
    }

    visitedPaths.add(current);
    long totalOutlets = 0;
    for (String neighbor : neighbors) {
      totalOutlets += countCertainPaths(neighbor, mappedStrings, visitedPaths, seenDac, seenFft);
    }
    visitedPaths.remove(current);

    // Store in cache
    cache.put(current, totalOutlets);
    return totalOutlets;
  }

  private static Map<String, Long> getCache(boolean seenDac, boolean seenFft) {
    if (seenDac && seenFft) return cacheWithBoth;
    if (seenDac) return cacheWithDac;
    if (seenFft) return cacheWithFft;
    return cacheWithNeither;
  }

  public static HashMap<String, List<String>> parseInput(String filename) throws IOException {
    HashMap<String, List<String>> parsedStrings = new HashMap<String, List<String>>();
    List<String> lines = Files.readAllLines(Path.of(filename));
    for (String line : lines) {
      String[] parts = line.split(":");
      String key = parts[0].trim();
      String value = parts[1].trim();
      List<String> valueList = Arrays.stream(value.split(" ")).toList();
      parsedStrings.put(key, valueList);
    }

    return parsedStrings;
  }
}
