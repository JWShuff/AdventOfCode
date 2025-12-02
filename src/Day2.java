import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.*;

public class Day2 {
  public static void main(String[] args) throws IOException {
    String filename = "./src/Day2Input.txt";
    long[][] ranges = getRanges(filename);
    long maxNum = getMaxNum(ranges);
    System.out.println("Number of ranges: " + ranges.length);
    System.out.println("Max value: " + maxNum);

    int maxHalfLength = String.valueOf(maxNum).length() / 2;

    List<Long> simpleCandidates = generatedDoubledNumbers(maxHalfLength);
    System.out.println("Number of candidates: " + simpleCandidates.size());

    long sum = simpleCandidates.stream()
            .filter(candidate -> isInAnyRange(candidate, ranges))
            .mapToLong(Long::longValue)
            .sum();
    System.out.println("Sum: " + sum);
    System.out.println("---Part Two---");

    int maxDigits = String.valueOf(maxNum).length();
    Set<Long> fullCandidates = generateRepeatedNumbers(maxDigits);
    System.out.println("Number of full candidates: " + fullCandidates.size());

    long fullSum = fullCandidates.stream()
            .filter(candidate -> isInAnyRange(candidate, ranges))
            .mapToLong(Long::longValue)
            .sum();
    System.out.println("Full Sum for Part Two: " + fullSum);
  }

  public static boolean isInAnyRange(long candidate, long[][] ranges) {
    for (long[] range : ranges) {
      if (candidate >= range[0] && candidate <= range[1]) {
        return true;
      }
    }
    return false;
  }

  public static List<Long> generatedDoubledNumbers(int maxHalfLength) {
    List<Long> doubledNumbers = new ArrayList<>();
    for (int len = 1; len <= maxHalfLength; len++) {
      long start = (len == 1) ? 1: (long) Math.pow(10, len - 1);
      long end = (long) Math.pow(10, len);

      for (long n = start; n < end; n++) {
        String s = String.valueOf(n);
        long doubled_num = Long.parseLong(s + s);
        doubledNumbers.add(doubled_num);
      }
    }

    return doubledNumbers;
  }

  public static Set<Long> generateRepeatedNumbers(int maxDigits) {
    Set<Long> repeatedNumbers = new HashSet<>();

    // for each pattern length k
    for (int k = 1; k <= maxDigits / 2; k++) {
      // for each repeat count r (at least 2)
      for (int r = 2; k * r <= maxDigits; r++) {
        // generate all k-digit patterns without leading zeros
        long start = (k == 1) ? 1 : (long) Math.pow(10, k - 1);
        long end = (long) Math.pow(10, k);

        for (long pattern = start; pattern < end; pattern++) {
          String s = String.valueOf(pattern);
          String repeatedStr = s.repeat(r);
          long num = Long.parseLong(repeatedStr);
          repeatedNumbers.add(num);
        }
      }
    }
    return repeatedNumbers;
  }

  public static long[][] getRanges(String filename) throws IOException {
    String line = Files.readString(Path.of(filename)).trim();
    long[][] ranges = Arrays.stream(line.split(","))
            .map( range -> {
              String[] parts = range.split("-");
              return new long[] {
                      Long.parseLong(parts[0]),
                      Long.parseLong(parts[1])
              };
            })
            .toArray(long[][]::new);
    return ranges;
  }

  public static long getMaxNum(long[][] ranges) throws IOException {
    return Arrays.stream(ranges)
            .flatMapToLong(Arrays::stream)
            .max()
            .orElse(0);
  }
}
