import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Day9 {
  public static void main(String[] args) throws IOException {
    System.out.println("Part 1 biggest possible rectangle " + calculateLargestRectangle("./src/Day9Input.txt"));
    System.out.println("Part 2 Largest legal rectangle " +  calculateLegalRectangle("./src/Day9Input.txt"));
  }

  public static long calculateLargestRectangle(String path) throws IOException {
    // Only ~500 coord pairs, so brute force isn't too bad.
    List<String> lines = Files.readAllLines(Path.of(path));

    List<int[]> coords = new ArrayList<>();
    for (String line : lines) {
      String[] parts = line.split(",");
      coords.add(new int[] {Integer.parseInt(parts[0]), Integer.parseInt(parts[1])});
    }
    List<Long> sizes = new ArrayList<>();
    long maxArea = 0;

    for (int i = 0; i < coords.size() - 1; i++) {
      for (int j = i + 1; j < coords.size(); j++) {
        int x1 =coords.get(i)[0];
        int y1 =coords.get(i)[1];
        int x2 =coords.get(j)[0];
        int y2 =coords.get(j)[1];
        // Add 1 for inclusive area
        maxArea = Math.max(maxArea,((long) (Math.abs(x2 - x1) + 1) * (Math.abs(y2 - y1) + 1)));
      }
    }
    return maxArea;
  }

  public static long calculateLegalRectangle(String path) throws IOException {
    List<String> lines = Files.readAllLines(Path.of(path));
    List<int[]> coords = new ArrayList<>();
    for (String line : lines) {
      String[] parts = line.split(",");
      coords.add(new int[] {Integer.parseInt(parts[0]), Integer.parseInt(parts[1])});
    }

    long maxArea = 0;
    for (int i = 0; i < coords.size() - 1; i++) {
      for (int j = i + 1; j < coords.size(); j++) {
        int x1 =coords.get(i)[0];
        int y1 =coords.get(i)[1];
        int x2 =coords.get(j)[0];
        int y2 =coords.get(j)[1];
        // Skip if same row or column, single width rectangle probably is not enough
        if (x1 == x2 || y1 == y2) continue;

        if (isValidRectangle(coords, x1, y1, x2, y2)) {
          long area = (long)(Math.abs(x2 - x1) + 1) * (Math.abs(y2 - y1) + 1);
          maxArea = Math.max(maxArea, area);
        }
      }
    }

    return maxArea;
  }

  // Check if a point is inside or on the polygon using ray casting
  private static boolean isInsidePolygon(List<int[]> polygon, int x, int y) {
    int n = polygon.size();
    int crossings = 0;

    for (int i = 0; i < n; i++) {
      int x1 = polygon.get(i)[0];
      int y1 = polygon.get(i)[1];
      int x2 = polygon.get((i + 1) % n)[0];
      int y2 = polygon.get((i + 1) % n)[1];

      // Check if point is exactly on this edge
      if (isOnSegment(x, y, x1, y1, x2, y2)) {
        return true;
      }

      // Ray casting: count crossings of horizontal ray to the right
      if ((y1 <= y && y < y2) || (y2 <= y && y < y1)) {
        // Calculate x-coordinate of intersection
        double intersectX = x1 + (double)(y - y1) / (y2 - y1) * (x2 - x1);
        if (x < intersectX) {
          crossings++;
        }
      }
    }

    return crossings % 2 == 1;
  }

  private static boolean isOnSegment(int px, int py, int x1, int y1, int x2, int y2) {
    // For axis-aligned segments only
    if (x1 == x2) { // Vertical segment
      return px == x1 && py >= Math.min(y1, y2) && py <= Math.max(y1, y2);
    } else if (y1 == y2) { // Horizontal segment
      return py == y1 && px >= Math.min(x1, x2) && px <= Math.max(x1, x2);
    }
    return false;
  }

  private static boolean isValidRectangle(List<int[]> polygon, int x1, int y1, int x2, int y2) {
    int minX = Math.min(x1, x2);
    int maxX = Math.max(x1, x2);
    int minY = Math.min(y1, y2);
    int maxY = Math.max(y1, y2);

    // Check all 4 corners are inside
    if (!isInsidePolygon(polygon, minX, minY) ||
            !isInsidePolygon(polygon, minX, maxY) ||
            !isInsidePolygon(polygon, maxX, minY) ||
            !isInsidePolygon(polygon, maxX, maxY)) {
      return false;
    }

    // Check that no polygon edge crosses through the rectangle interior
    int n = polygon.size();
    for (int i = 0; i < n; i++) {
      int px1 = polygon.get(i)[0];
      int py1 = polygon.get(i)[1];
      int px2 = polygon.get((i + 1) % n)[0];
      int py2 = polygon.get((i + 1) % n)[1];

      // Check if this polygon edge passes through the interior of the rectangle
      if (edgeCrossesRectangleInterior(px1, py1, px2, py2, minX, maxX, minY, maxY)) {
        return false;
      }
    }

    return true;
  }

  private static boolean edgeCrossesRectangleInterior(int px1, int py1, int px2, int py2,
                                                      int minX, int maxX, int minY, int maxY) {
    // For axis-aligned polygon edges:
    if (px1 == px2) { // Vertical edge
      // Does it pass through the interior (not on boundary)?
      if (px1 > minX && px1 < maxX) {
        // Check if the y-range of the edge overlaps with rectangle's y-range
        int edgeMinY = Math.min(py1, py2);
        int edgeMaxY = Math.max(py1, py2);
        // Overlaps interior if ranges intersect
        if (edgeMinY < maxY && edgeMaxY > minY) {
          return true;
        }
      }
    } else if (py1 == py2) { // Horizontal edge
      if (py1 > minY && py1 < maxY) {
        int edgeMinX = Math.min(px1, px2);
        int edgeMaxX = Math.max(px1, px2);
        if (edgeMinX < maxX && edgeMaxX > minX) {
          return true;
        }
      }
    }
    return false;
  }
}
