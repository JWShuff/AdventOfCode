import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Day12 {

  public static void main(String[] args) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader("./src/Day12Input.txt"));
    List<GridSpec> grids = new ArrayList<>();
    List<List<String>> shapes = new ArrayList<>();

    parseBaseInputs(grids, shapes, reader);
    System.out.println("Parsed " + shapes.size() + " shapes:");
    for (int i = 0; i < shapes.size(); i++) {
      System.out.println("Shape " + i + ":");
      for (String row : shapes.get(i)) {
        System.out.println("  " + row);
      }
    }

    int[] shapeSizes = countShapeSizes(shapes);

    System.out.println("Shape Sizes are: " + Arrays.toString(shapeSizes));
    removeImpossibleGrids(grids, shapeSizes);

    System.out.println("Total grids: " + grids.size());

    List<List<Coord>> coords = convertToCoords(shapes);
    System.out.println("An example for shape 0 coords are: " + coords.get(0).toString());
    List<List<List<Coord>>> allCoordOrientations = generateAllOrientations(coords);

    System.out.println("Shape 4 has " + allCoordOrientations.get(4).size() + " orientations:");
    for (List<Coord> orient : allCoordOrientations.get(4)) {
      System.out.println("  " + orient);
    }

    int solveableCount = 0;
    int gridNum = 0;
    for (GridSpec g : grids) {
      gridNum++;
      boolean[][] grid = new boolean[g.height()][g.width()];
      int[] counts = g.quantities().clone();
      int totalPieces = Arrays.stream(counts).sum();
      System.out.println("Grid " + gridNum + "/" + grids.size() +
              ": " + g.width() + "x" + g.height() +
              " with " + totalPieces + " pieces");

      if (solve(grid, counts, allCoordOrientations)) {
        solveableCount++;
        System.out.println("  -> SOLVABLE");
      } else {
        System.out.println("  -> impossible");
      }
    }

    System.out.println("Solvable: " + solveableCount);
  }

  private static boolean solve(boolean[][] grid, int[] pieceCounts, List<List<List<Coord>>> allOrientations) {
    // Check if all pieces are placed
    boolean allPlaced = true;
    for (int count : pieceCounts) {
      if (count > 0) {
        allPlaced = false;
        break;
      }
    }
    if (allPlaced) return true;

    // Find first piece type that still needs placing
    int shapeIdx = -1;
    for (int i = 0; i < pieceCounts.length; i++) {
      if (pieceCounts[i] > 0) {
        shapeIdx = i;
        break;
      }
    }

    // Try placing this piece at every valid position
    for (List<Coord> orientation : allOrientations.get(shapeIdx)) {
      for (int r = 0; r < grid.length; r++) {
        for (int c = 0; c < grid[0].length; c++) {
          if (canPlace(grid, orientation, r, c)) {
            place(grid, orientation, r, c, true);
            pieceCounts[shapeIdx]--;

            if (solve(grid, pieceCounts, allOrientations)) {
              return true;
            }

            place(grid, orientation, r, c, false);
            pieceCounts[shapeIdx]++;
          }
        }
      }
    }
    return false;
  }

  private static boolean hasDeadRegions(boolean[][] grid, int[] pieceCounts) {
    // Find smallest piece we still need to place
    int[] shapeSizes = {5, 7, 6, 7, 7, 7}; // hardcoding, its okay.
    int minNeeded = Integer.MAX_VALUE;
    for (int i = 0; i < pieceCounts.length; i++) {
      if (pieceCounts[i] > 0) {
        minNeeded = Math.min(minNeeded, shapeSizes[i]);
      }
    }
    if (minNeeded == Integer.MAX_VALUE) return false;  // No pieces left

    // Find all empty cells, then flood fill to find isolated regions
    boolean[][] visited = new boolean[grid.length][grid[0].length];

    for (int r = 0; r < grid.length; r++) {
      for (int c = 0; c < grid[0].length; c++) {
        if (!grid[r][c] && !visited[r][c]) {
          int regionSize = floodFill(grid, visited, r, c);
          if (regionSize < minNeeded) {
            return true;  // Dead region found!
          }
        }
      }
    }
    return false;
  }

  private static int floodFill(boolean[][] grid, boolean[][] visited, int r, int c) {
    if (r < 0 || r >= grid.length || c < 0 || c >= grid[0].length) return 0;
    if (grid[r][c] || visited[r][c]) return 0;

    visited[r][c] = true;
    return 1 + floodFill(grid, visited, r-1, c)
            + floodFill(grid, visited, r+1, c)
            + floodFill(grid, visited, r, c-1)
            + floodFill(grid, visited, r, c+1);
  }

  private static int[] findFirstEmpty(boolean[][] grid) {
    for (int row = 0; row < grid.length; row++) {
      for (int col = 0; col < grid[0].length; col++) {
        if (!grid[row][col]) {
          return new int[]{row, col};
        }
      }
    }
    // grid is full.
    return null;
  }

  private static boolean canPlace(boolean[][] grid,
                                  List<Coord> piece,
                                  int offsetRow,
                                  int offsetCol) {
    for (Coord c : piece) {
      int row = c.row() +  offsetRow;
      int col = c.col() + offsetCol;
      // out of bounds?
      if (row < 0 || row >= grid.length || col < 0 || col >= grid[0].length) {
        return false;
      }
      // Already occupied?
      if (grid[row][col]) {
        return false;
      }
    }
    return true;
  }

  private static void place(boolean[][] grid, List<Coord> piece,
                            int offsetRow, int offsetCol, boolean value) {
    for (Coord c : piece) {
      int row = c.row() + offsetRow;
      int col = c.col() + offsetCol;
      grid[row][col] = value;
    }
  }

  private static List<List<List<Coord>>> generateAllOrientations(List<List<Coord>> baseCoords) {
    List<List<List<Coord>>> allOrientations = new ArrayList<>();

    for (List<Coord> shape : baseCoords) {
      Set<List<Coord>> uniqueOrientations = new HashSet<>();

      List<Coord> current = shape;
      // 4 rotations of original
      for (int r = 0; r < 4; r++) {
        uniqueOrientations.add(sortAndNormalize(current));
        current = rotate90(current);
      }

      // flip, then 4 rotations of flipped
      current = flip(shape);
      for (int r = 0; r < 4; r++) {
        uniqueOrientations.add(sortAndNormalize(current));
        current = rotate90(current);
      }

      allOrientations.add(new ArrayList<>(uniqueOrientations));
    }
    return allOrientations;
  }

  private static List<Coord> sortAndNormalize(List<Coord> coords) {
    List<Coord> normalized = normalize(coords);
    // Sort so that identical shapes get deduplicated by the Set
    normalized.sort(Comparator.comparingInt(Coord::row).thenComparingInt(Coord::col));
    return normalized;
  }

  private static List<List<Coord>> convertToCoords(List<List<String>> shapes) {
    List<List<Coord>> coords = new ArrayList<>();
    for (List<String> shape : shapes) {
      List<Coord> coord = new ArrayList<>();
      for (int row = 0; row < shape.size(); row++) {
        String line =  shape.get(row);
        for (int col = 0; col < line.length(); col++) {
          if (line.charAt(col) == '#') {
            coord.add(new Coord(row, col));
          }
        }
      }
      coords.add(coord);
    }
    return coords;
  }

  private static List<Coord> rotate90(List<Coord> coords) {
    // Rotate: (row, col) -> (col, -row)
    List<Coord> rotated = new ArrayList<>();
    for (Coord c : coords) {
      rotated.add(new Coord(c.col(), -c.row()));
    }
    return normalize(rotated);
  }

  private static List<Coord> flip(List<Coord> coords) {
    // Flip horizontally: (row, col) -> (row, -col)
    List<Coord> flipped = new ArrayList<>();
    for (Coord c : coords) {
      flipped.add(new Coord(c.row(), -c.col()));
    }
    return normalize(flipped);
  }

  private static List<Coord> normalize(List<Coord> coords) {
    int minRow = coords.stream().mapToInt(Coord::row).min().orElse(0);
    int minCol = coords.stream().mapToInt(Coord::col).min().orElse(0);

    List<Coord> normalized = new ArrayList<>();
    for (Coord c : coords) {
      normalized.add(new Coord(c.row() - minRow, c.col() - minCol));
    }
    return normalized;
  }

  private static void removeImpossibleGrids(List<GridSpec> grids, int[] shapeSizes) {
    grids.removeIf(g -> {
      int gridArea = g.width() * g.height();
      int neededArea = 0;
      for (int i = 0; i < 6; i++) {
        neededArea += g.quantities()[i] * shapeSizes[i];
      }
      return neededArea > gridArea;
    });
  }

  record GridSpec(int width, int height, int[] quantities) {}
  record Coord(int row, int col) {}

  public static void parseBaseInputs(List<GridSpec> grids,
                                     List<List<String>> shapes,
                                     BufferedReader reader) throws IOException {
    String line;

    while ((line = reader.readLine()) != null) {
      if (line.matches("\\d+:")) {
        // Shape def line
        List<String> shape =  new ArrayList<>();
        shape.add(reader.readLine()); // line 0
        shape.add(reader.readLine()); // line 1
        shape.add(reader.readLine()); // line 2 of shape
        shapes.add(shape);
      } else if (line.matches("\\d+x\\d+:.*")) {
        // This is a grid line
        // Split on x and the colon giving us parts[0] width parts[1] height and parts[2] the shape counts
        String[] parts = line.split("[x:]");
        int width = Integer.parseInt(parts[0].trim());
        int height = Integer.parseInt(parts[1].trim());
        int[] quantities = Arrays.stream(parts[2].trim().split(" ")).mapToInt(Integer::parseInt).toArray();
        grids.add(new GridSpec(width, height, quantities));
      }
    }
  }

  public static int[] countShapeSizes(List<List<String>> shapes) {
    int[] shapeSizes = new int[shapes.size()];
    for (int i = 0; i < shapes.size(); i++) {
      int count = 0;
      for (String row : shapes.get(i)) {
        for (char c : row.toCharArray()) {
          if (c == '#') count++;
        }
      }
      shapeSizes[i] = count;
    }
    return shapeSizes;
  }
}
