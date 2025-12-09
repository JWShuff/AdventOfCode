import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Day8 {
  public static void main(String[] args) throws IOException {
    long result = connectJunctions("./src/Day8Input.txt", 1000);
    System.out.println("Part 1 Largest 3 Circuit Multiplied: " + result);

    long lastDistanceMultiplied = connectAllJunctions("./src/Day8Input.txt");
    System.out.println("Part 2 last connection x multiplied: " + lastDistanceMultiplied);
  }

  public static long connectJunctions(String filename, int connections) throws IOException {
    List<String> lines = Files.readAllLines(Path.of(filename));
    int n = lines.size();

    int[][] coords = new int[n][3];
    for (int i = 0; i < n; i++) {
      String[] parts = lines.get(i).split(",");
      coords[i][0] = Integer.parseInt(parts[0]);
      coords[i][1] = Integer.parseInt(parts[1]);
      coords[i][2] = Integer.parseInt(parts[2]);
    }

    // calculate all paired distances and store (distance, i, j)
    // Store distance squared to dodge sqrt
    List<long[]> pairs =  new ArrayList<>();
    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        long dx = coords[i][0] - coords[j][0];
        long dy = coords[i][1] - coords[j][1];
        long dz = coords[i][2] - coords[j][2];
        long distsq = dx * dx + dy * dy + dz * dz;
        pairs.add(new long[]{distsq, i, j});
      }
    }

    // sort by distance
    pairs.sort(Comparator.comparingLong(pair -> pair[0]));

    int[] parent = new int[n];
    int[] rank = new int[n];
    for (int i = 0; i < n; i++) {
      parent[i] = i;
    }

    // Connect the closest 1000
    int connected = 0;
    for (long[] pair : pairs) {
      if (connected >= connections) break;
      int i = (int) pair[1];
      int j = (int) pair[2];
      union(parent, rank, i, j);
      connected++;
    }

    // Count the circuits
    Map<Integer, Integer> circuitSizes = new HashMap<>();
    for (int i = 0; i < n; i++) {
      int root = find(parent, i);
      circuitSizes.merge(root, 1, Integer::sum);
    }

    // Get the three largest circuits
    List<Integer> sizes = new ArrayList<>(circuitSizes.values());
    sizes.sort(Collections.reverseOrder());

    return (long) sizes.get(0) * sizes.get(1) * sizes.get(2);
  }

  public static long connectAllJunctions(String filename) throws IOException {
    List<String> lines = Files.readAllLines(Path.of(filename));
    int n = lines.size();
    int[][] coords = new int[n][3];
    for (int i = 0; i < n; i++) {
      String[] parts = lines.get(i).split(",");
      coords[i][0] = Integer.parseInt(parts[0]);
      coords[i][1] = Integer.parseInt(parts[1]);
      coords[i][2] = Integer.parseInt(parts[2]);
    }

    List<long[]> pairs =  new ArrayList<>();
    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        long dx = coords[i][0] - coords[j][0];
        long dy = coords[i][1] - coords[j][1];
        long dz = coords[i][2] - coords[j][2];
        long distsq = dx * dx + dy * dy + dz * dz;
        pairs.add(new long[]{distsq, i, j});
      }
    }

    pairs.sort(Comparator.comparingLong(pair -> pair[0]));
    int[] parent = new int[n];
    int[] rank = new int[n];
    for (int i = 0; i < n; i++) parent[i] = i;
    int numCircuits = n; // We start with the total of circuits
    int lastI = -1, lastJ = -1;
    for (long[] pair : pairs) {
      int i = (int) pair[1];
      int j = (int) pair[2];

      // Only handle if these are different circuits
      if (find(parent, i) != find(parent, j)) {
        union(parent, rank, i, j);
        numCircuits--;
        lastI = i;
        lastJ = j;

        if (numCircuits == 1) break;
      }
    }

    return (long) coords[lastI][0] * coords[lastJ][0];
  }

  private static int find(int[] parent, int x) {
    if (parent[x] != x) {
      parent[x] = find(parent, parent[x]);
    }
    return parent[x];
  }

  private static void union(int[] parent, int[] rank, int x, int y) {
    int rootX = find(parent, x);
    int rootY = find(parent, y);

    if (rootX != rootY) {
      if (rank[rootX] < rank[rootY]) {
        parent[rootX] = rootY;
      } else if (rank[rootX] > rank[rootY]) {
        parent[rootY] = rootX;
      } else {
        parent[rootY] = rootX;
        rank[rootX]++; // promote its rank
      }
    }
  }
}
