import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;

public class Day10 {

  /**
   * Represents a single machine with its target state and buttons.
   */
  static class Machine {
    boolean[] target;        // What state we want the lights in (true = ON)
    List<int[]> buttons;     // Each button is a list of light indices it toggles
    int[] joltages;          // Joltage requirements for Part 2

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Target: [");
      for (boolean b : target) sb.append(b ? '#' : '.');
      sb.append("] (").append(target.length).append(" lights)\n");
      sb.append("Buttons (").append(buttons.size()).append("): ");
      for (int[] btn : buttons) {
        sb.append(Arrays.toString(btn)).append(" ");
      }
      sb.append("\nJoltages: ").append(Arrays.toString(joltages));
      return sb.toString();
    }
  }

  /**
   * Parse a single line into a Machine object.
   */
  static Machine parseLine(String line) {
    Machine m = new Machine();

    Pattern targetPattern = Pattern.compile("\\[([.#]+)\\]");
    Matcher targetMatcher = targetPattern.matcher(line);
    if (targetMatcher.find()) {
      String targetStr = targetMatcher.group(1);
      m.target = new boolean[targetStr.length()];
      for (int i = 0; i < targetStr.length(); i++) {
        m.target[i] = (targetStr.charAt(i) == '#');
      }
    }

    m.buttons = new ArrayList<>();
    Pattern buttonPattern = Pattern.compile("\\(([0-9,]+)\\)");
    Matcher buttonMatcher = buttonPattern.matcher(line);
    while (buttonMatcher.find()) {
      String buttonStr = buttonMatcher.group(1);
      String[] parts = buttonStr.split(",");
      int[] indices = new int[parts.length];
      for (int i = 0; i < parts.length; i++) {
        indices[i] = Integer.parseInt(parts[i].trim());
      }
      m.buttons.add(indices);
    }

    Pattern joltagePattern = Pattern.compile("\\{([0-9,]+)\\}");
    Matcher joltageMatcher = joltagePattern.matcher(line);
    if (joltageMatcher.find()) {
      String joltageStr = joltageMatcher.group(1);
      String[] parts = joltageStr.split(",");
      m.joltages = new int[parts.length];
      for (int i = 0; i < parts.length; i++) {
        m.joltages[i] = Integer.parseInt(parts[i].trim());
      }
    }

    return m;
  }

  // ==========================================================================
  // PART 1: GF(2) Linear Algebra
  // ==========================================================================

  static int findMinimumPresses(Machine m) {
    int numLights = m.target.length;
    int numButtons = m.buttons.size();

    int[][] matrix = new int[numLights][numButtons + 1];

    for (int btnIdx = 0; btnIdx < numButtons; btnIdx++) {
      int[] affectedLights = m.buttons.get(btnIdx);
      for (int lightIdx : affectedLights) {
        matrix[lightIdx][btnIdx] = 1;
      }
    }

    for (int i = 0; i < numLights; i++) {
      matrix[i][numButtons] = m.target[i] ? 1 : 0;
    }

    int[] pivotRow = new int[numButtons];
    Arrays.fill(pivotRow, -1);

    int currentRow = 0;
    for (int col = 0; col < numButtons && currentRow < numLights; col++) {
      int pivotIdx = -1;
      for (int row = currentRow; row < numLights; row++) {
        if (matrix[row][col] == 1) {
          pivotIdx = row;
          break;
        }
      }

      if (pivotIdx == -1) continue;

      if (pivotIdx != currentRow) {
        int[] temp = matrix[currentRow];
        matrix[currentRow] = matrix[pivotIdx];
        matrix[pivotIdx] = temp;
      }

      pivotRow[col] = currentRow;

      for (int row = 0; row < numLights; row++) {
        if (row != currentRow && matrix[row][col] == 1) {
          for (int c = 0; c <= numButtons; c++) {
            matrix[row][c] ^= matrix[currentRow][c];
          }
        }
      }

      currentRow++;
    }

    List<Integer> freeColumns = new ArrayList<>();
    for (int col = 0; col < numButtons; col++) {
      if (pivotRow[col] == -1) {
        freeColumns.add(col);
      }
    }

    int numFree = freeColumns.size();

    for (int row = 0; row < numLights; row++) {
      boolean allZero = true;
      for (int col = 0; col < numButtons; col++) {
        if (matrix[row][col] == 1) {
          allZero = false;
          break;
        }
      }
      if (allZero && matrix[row][numButtons] == 1) {
        return Integer.MAX_VALUE;
      }
    }

    int minPresses = Integer.MAX_VALUE;

    for (int mask = 0; mask < (1 << numFree); mask++) {
      int[] solution = new int[numButtons];

      for (int i = 0; i < numFree; i++) {
        int freeCol = freeColumns.get(i);
        solution[freeCol] = (mask >> i) & 1;
      }

      for (int col = numButtons - 1; col >= 0; col--) {
        if (pivotRow[col] == -1) continue;

        int row = pivotRow[col];
        int value = matrix[row][numButtons];
        for (int c = 0; c < numButtons; c++) {
          if (c != col) {
            value ^= matrix[row][c] * solution[c];
          }
        }
        solution[col] = value;
      }

      int presses = 0;
      for (int v : solution) {
        presses += v;
      }

      minPresses = Math.min(minPresses, presses);
    }

    return minPresses;
  }

  // ==========================================================================
  // PART 2: Integer Linear Algebra
  // ==========================================================================
  //
  // Now we're solving A × x = b over the integers (not GF(2)).
  // - x[i] = number of times to press button i (must be >= 0)
  // - We want to minimize sum(x[i])
  //
  // Strategy:
  // 1. Gaussian elimination over rationals to find solution structure
  // 2. Identify free variables
  // 3. Search for non-negative integer solutions with minimum sum
  //
  // The tricky part: free variables can now be ANY non-negative integer,
  // not just 0 or 1. So we can't enumerate all 2^k combinations.
  //
  // However, if the system is well-constrained (few free variables), and
  // the target values are small, we might be able to bound the search space.

  /**
   * Find minimum button presses for Part 2 (joltage counters).
   * 
   * This is solving A × x = b where:
   *   - A[i][j] = 1 if button j affects counter i, else 0
   *   - b[i] = joltage requirement for counter i
   *   - x[j] = number of times to press button j (>= 0)
   *   - Minimize sum(x)
   */
  static long findMinimumPressesJoltage(Machine m) {
    int numCounters = m.joltages.length;
    int numButtons = m.buttons.size();

    // =========================================================================
    // STEP 1: Build the augmented matrix [A | b] using doubles for now
    // =========================================================================
    
    double[][] matrix = new double[numCounters][numButtons + 1];

    // Fill in which buttons affect which counters
    for (int btnIdx = 0; btnIdx < numButtons; btnIdx++) {
      int[] affectedCounters = m.buttons.get(btnIdx);
      for (int counterIdx : affectedCounters) {
        if (counterIdx < numCounters) {
          matrix[counterIdx][btnIdx] = 1.0;
        }
      }
    }

    // Fill in the target column (joltage requirements)
    for (int i = 0; i < numCounters; i++) {
      matrix[i][numButtons] = m.joltages[i];
    }

    // =========================================================================
    // STEP 2: Gaussian Elimination over Rationals (using doubles)
    // =========================================================================
    //
    // Unlike GF(2), we now:
    //   - Divide to make the pivot = 1
    //   - Subtract multiples (not XOR) to eliminate
    
    int[] pivotRow = new int[numButtons];
    int[] pivotCol = new int[numCounters];  // Which column is the pivot for each row
    Arrays.fill(pivotRow, -1);
    Arrays.fill(pivotCol, -1);

    int currentRow = 0;
    for (int col = 0; col < numButtons && currentRow < numCounters; col++) {
      // Find the best pivot (largest absolute value for numerical stability)
      int pivotIdx = -1;
      double maxVal = 1e-10;  // Threshold for "zero"
      for (int row = currentRow; row < numCounters; row++) {
        if (Math.abs(matrix[row][col]) > maxVal) {
          maxVal = Math.abs(matrix[row][col]);
          pivotIdx = row;
        }
      }

      if (pivotIdx == -1) {
        // No pivot in this column - this button is a "free variable"
        continue;
      }

      // Swap the pivot row to the current row
      if (pivotIdx != currentRow) {
        double[] temp = matrix[currentRow];
        matrix[currentRow] = matrix[pivotIdx];
        matrix[pivotIdx] = temp;
      }

      // Record pivot information
      pivotRow[col] = currentRow;
      pivotCol[currentRow] = col;

      // Scale the pivot row so the pivot element becomes 1
      double pivotVal = matrix[currentRow][col];
      for (int c = 0; c <= numButtons; c++) {
        matrix[currentRow][c] /= pivotVal;
      }

      // Eliminate: subtract multiples of pivot row from all other rows
      for (int row = 0; row < numCounters; row++) {
        if (row != currentRow && Math.abs(matrix[row][col]) > 1e-10) {
          double factor = matrix[row][col];
          for (int c = 0; c <= numButtons; c++) {
            matrix[row][c] -= factor * matrix[currentRow][c];
          }
        }
      }

      currentRow++;
    }

    // =========================================================================
    // STEP 3: Check for inconsistency
    // =========================================================================
    
    for (int row = 0; row < numCounters; row++) {
      boolean allZero = true;
      for (int col = 0; col < numButtons; col++) {
        if (Math.abs(matrix[row][col]) > 1e-10) {
          allZero = false;
          break;
        }
      }
      if (allZero && Math.abs(matrix[row][numButtons]) > 1e-10) {
        // No solution exists
        return Long.MAX_VALUE;
      }
    }

    // =========================================================================
    // STEP 4: Identify free variables
    // =========================================================================
    
    List<Integer> freeColumns = new ArrayList<>();
    List<Integer> pivotColumns = new ArrayList<>();
    for (int col = 0; col < numButtons; col++) {
      if (pivotRow[col] == -1) {
        freeColumns.add(col);
      } else {
        pivotColumns.add(col);
      }
    }

    int numFree = freeColumns.size();
    
    // Debug output for first few machines
    // System.out.println("  Free variables: " + numFree + ", Pivot variables: " + pivotColumns.size());

    // =========================================================================
    // STEP 5: Search for minimum non-negative integer solution
    // =========================================================================
    //
    // For each assignment of free variables, the pivot variables are determined:
    //   pivot_var = target - sum(coeff * free_var)
    //
    // We need all variables (both pivot and free) to be non-negative integers.
    //
    // If numFree is small and values are bounded, we can search.
    // Let's estimate a bound: max joltage value might be a reasonable upper bound.

    int maxJoltage = 0;
    for (int j : m.joltages) {
      maxJoltage = Math.max(maxJoltage, j);
    }

    // If no free variables, there's exactly one solution - just read it off
    if (numFree == 0) {
      long totalPresses = 0;
      for (int col : pivotColumns) {
        int row = pivotRow[col];
        long value = Math.round(matrix[row][numButtons]);
        if (value < 0) {
          return Long.MAX_VALUE;  // No non-negative solution
        }
        totalPresses += value;
      }
      return totalPresses;
    }

    // With free variables, we need to search
    // Bound each free variable: it can't exceed the max joltage (heuristic)
    // This could be slow if numFree is large or maxJoltage is huge
    
    long minPresses = Long.MAX_VALUE;
    
    // For now, let's try a bounded search
    // If this is too slow, we'll need a smarter approach (e.g., ILP solver)
    
    // Upper bound for each free variable
    // We need to be careful here - the bound needs to be large enough
    // that pivot variables can become non-negative.
    // Previously, we tried 100, and that was too low. Using this approach and guarding at 100M gives us a successful answer
    int bound = 0;
    for (int row = 0; row < numCounters; row++) {
      bound = Math.max(bound, (int) Math.ceil(Math.abs(matrix[row][numButtons])) + 1);
    }
    // Also consider that coefficients might require larger values
    bound = Math.max(bound, maxJoltage + 1);
    
    // Total combinations = bound^numFree
    // If this is too large, we need a different approach
    long totalCombinations = 1;
    for (int i = 0; i < numFree; i++) {
      totalCombinations *= bound;
    }
    
    if (totalCombinations > 100_000_000) {
      // Too many combinations - this shouldn't happen for well-designed AoC
      System.out.println("WARNING: Too many combinations (" + totalCombinations + ") for machine with " + numFree + " free vars, bound=" + bound);
      // Try with smaller bound anyway
      bound = 300;
    }
    
    // Debug: track if we find any valid solution
    boolean foundAny = false;

    // Enumerate all combinations of free variables
    int[] freeValues = new int[numFree];
    
    // Early termination optimization: track the minimum sum found so far
    // If current free variable sum already exceeds our best, skip
    
    while (true) {
      // Compute pivot variables based on current free variable assignment
      long[] solution = new long[numButtons];
      boolean valid = true;
      
      // Set free variables
      long freeSum = 0;
      for (int i = 0; i < numFree; i++) {
        solution[freeColumns.get(i)] = freeValues[i];
        freeSum += freeValues[i];
      }
      
      // Early termination: if free variables alone exceed best solution, skip
      if (freeSum >= minPresses) {
        valid = false;
      } else {
        // Compute pivot variables using back-substitution
        for (int col : pivotColumns) {
          int row = pivotRow[col];
          double value = matrix[row][numButtons];
          
          // Subtract contributions from other variables
          for (int c = 0; c < numButtons; c++) {
            if (c != col && Math.abs(matrix[row][c]) > 1e-10) {
              value -= matrix[row][c] * solution[c];
            }
          }
          
          // Round to nearest integer
          long intValue = Math.round(value);
          
          // Check if it's actually an integer (within tolerance)
          if (Math.abs(value - intValue) > 1e-6) {
            valid = false;
            break;
          }
          
          // Check non-negativity
          if (intValue < 0) {
            valid = false;
            break;
          }
          
          solution[col] = intValue;
        }
      }
      
      if (valid) {
        foundAny = true;
        // Count total presses
        long presses = 0;
        for (long v : solution) {
          presses += v;
        }
        minPresses = Math.min(minPresses, presses);
      }
      
      // Increment free variable values (like counting in base 'bound')
      int idx = 0;
      while (idx < numFree) {
        freeValues[idx]++;
        if (freeValues[idx] < bound) {
          break;
        }
        freeValues[idx] = 0;
        idx++;
      }
      if (idx == numFree) {
        break;  // All combinations exhausted
      }
    }

    return minPresses;
  }

  public static void main(String[] args) throws IOException {
    // Solve Part 1
    List<String> lines = Files.readAllLines(Path.of("src/Day10Input.txt"));
    List<Machine> machines = new ArrayList<>();
    for (String line : lines) {
      if (!line.isBlank()) {
        machines.add(parseLine(line));
      }
    }

    int totalPart1 = 0;
    for (Machine m : machines) {
      totalPart1 += findMinimumPresses(m);
    }
    System.out.println("Part 1: " + totalPart1 + "\n");

    // Solve Part 2
    System.out.println("=== Solving Part 2 Actual Input ===\n");

    // First, let's analyze the structure of the problems
    System.out.println("Analyzing problem structure...");
    int maxFreeVars = 0;
    int maxJoltageValue = 0;
    for (int i = 0; i < machines.size(); i++) {
      Machine m = machines.get(i);
      for (int j : m.joltages) {
        maxJoltageValue = Math.max(maxJoltageValue, j);
      }
    }
    System.out.println("Max joltage value across all machines: " + maxJoltageValue);
    System.out.println();

    long totalPart2 = 0;
    for (int i = 0; i < machines.size(); i++) {
      Machine m = machines.get(i);
      long presses = findMinimumPressesJoltage(m);

      if (presses == Long.MAX_VALUE) {
        System.out.println("WARNING: Machine " + (i + 1) + " has no solution!");
        System.out.println("  " + m);
      } else {
        totalPart2 += presses;
      }

      if ((i + 1) % 20 == 0) {
        System.out.println("  Processed " + (i + 1) + "/" + machines.size() + " machines...");
      }
    }

    System.out.println("\nPart 2: " + totalPart2);

  }
}
