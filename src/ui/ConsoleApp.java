package ui;

import algorithms.*;
import benchmark.BenchmarkResult;
import benchmark.BenchmarkRunner;
import data.NetworkLoader;
import data.SampleNetworks;
import data.TNTPNetworkLoader;
import model.FlowEdge;
import model.FlowNetwork;
import model.FlowResult;

import java.io.File;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive Console Application for the Network Flow Optimization System.
 * Incorporates the Sioux Falls TNTP dataset suite (net, node, trips).
 */
public class ConsoleApp {
    private final Scanner scanner = new Scanner(System.in);
    private FlowNetwork currentNetwork;
    private String currentNetworkName;
    private int currentSource;
    private int currentSink;

    public ConsoleApp() {
        loadDefaultNetwork();
    }

    private void loadDefaultNetwork() {
        this.currentNetwork = SampleNetworks.createSiouxFallsNetwork();
        this.currentNetworkName = "Sioux Falls Network (24 Nodes, 76 Links) [TNTP Dataset]";
        this.currentSource = 0;   // Zone 1
        this.currentSink = 23;    // Zone 24
    }

    public void start() {
        printBanner();

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> selectPredefinedNetwork();
                case "2" -> changeOriginDestinationPair();
                case "3" -> runComparativeBenchmark();
                case "4" -> runSingleAlgorithmDetailed();
                case "5" -> displayMinCutBottlenecks();
                case "6" -> loadNetworkFromFile();
                case "7" -> createCustomNetworkInteractively();
                case "8" -> launchGUI();
                case "9" -> displayCurrentNetworkInfo();
                case "0" -> {
                    System.out.println("\nExiting Network Flow Optimization System. Good luck with DSA-3!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please enter a number (0 - 9).");
            }
        }
    }

    private void printBanner() {
        System.out.println("\n" + "=".repeat(78));
        System.out.println("           NETWORK FLOW OPTIMIZATION SYSTEM (DSA-3: 25CS2103E)");
        System.out.println("   Datasets: Sioux Falls (net, node, trips) | 12-Node Benchmark | Telecom");
        System.out.println("   Algorithms: Ford-Fulkerson | Edmonds-Karp | Dinic's Algorithm | Min-Cut");
        System.out.println("=".repeat(78));
    }

    private void printMainMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.printf("Active Network: [%s]\n", currentNetworkName);
        System.out.printf("Endpoints:      Source (S) = %s (Index %d) -> Sink (T) = %s (Index %d)\n",
                currentNetwork.getVertexName(currentSource), currentSource,
                currentNetwork.getVertexName(currentSink), currentSink);
        System.out.printf("Graph Size:     %d Vertices, %d Directed Edges\n",
                currentNetwork.getNumVertices(), currentNetwork.getNumEdges());
        System.out.println("------------------------------------------------------------------------------");
        System.out.println("1. Select Predefined Scenario (Sioux Falls TNTP, 12-Node, Telecom, Traffic)");
        System.out.println("2. Change Source (Origin) & Sink (Destination) Pair");
        System.out.println("3. Run Comparative Benchmark (Ford-Fulkerson vs Edmonds-Karp vs Dinic)");
        System.out.println("4. Run Single Algorithm with Step-by-Step Augmenting Paths");
        System.out.println("5. Analyze Minimum Cut & Critical Bottleneck Road Segments");
        System.out.println("6. Load Network from Text File");
        System.out.println("7. Build Custom Network (Interactive Input)");
        System.out.println("8. Launch Swing Graphical Visualizer (GUI with Map Layout)");
        System.out.println("9. View Current Network Graph Details");
        System.out.println("0. Exit");
        System.out.print("Enter choice: ");
    }

    private void selectPredefinedNetwork() {
        System.out.println("\n--- Select Predefined Scenario ---");
        System.out.println("1. Sioux Falls Road Network (24 Nodes, 76 Links) [Zone 1 -> Zone 24]");
        System.out.println("2. Sioux Falls System-Wide Demand Network (360k Trips OD Matrix, Super-Source/Sink)");
        System.out.println("3. 12-Node Benchmark Network (Course Presentation Test Case: Slide 8)");
        System.out.println("4. Telecommunication & Data Center Bandwidth Allocation");
        System.out.println("5. Urban Traffic Flow Network (Small 6-Node Demo)");
        System.out.println("6. Municipal Water Distribution Pipeline System");
        System.out.print("Choose (1-6): ");
        String sel = scanner.nextLine().trim();

        switch (sel) {
            case "1" -> {
                currentNetwork = SampleNetworks.createSiouxFallsNetwork();
                currentNetworkName = "Sioux Falls Road Network (24 Nodes, 76 Links) [TNTP Dataset]";
                currentSource = 0;   // Zone 1
                currentSink = 23;    // Zone 24
                System.out.println("✔ Loaded Sioux Falls Network (Point-to-Point: Zone 1 -> Zone 24).");
            }
            case "2" -> {
                currentNetwork = SampleNetworks.createSiouxFallsSystemWideNetwork();
                currentNetworkName = "Sioux Falls System-Wide Demand (360,600 OD Trips)";
                currentSource = 24;  // Super-Source
                currentSink = 25;    // Super-Sink
                System.out.println("✔ Loaded Sioux Falls System-Wide Multi-Demand Network with Super-Source and Super-Sink.");
            }
            case "3" -> {
                currentNetwork = SampleNetworks.create12NodeBenchmarkNetwork();
                currentNetworkName = "12-Node Benchmark Network (Slide 8)";
                currentSource = 0;
                currentSink = 11;
                System.out.println("✔ Loaded 12-Node Benchmark Network.");
            }
            case "4" -> {
                currentNetwork = SampleNetworks.createTelecommunicationsNetwork();
                currentNetworkName = "Telecom & Data Center Bandwidth Network";
                currentSource = 0;
                currentSink = 7;
                System.out.println("✔ Loaded Telecommunication Network.");
            }
            case "5" -> {
                currentNetwork = SampleNetworks.createUrbanTrafficNetwork();
                currentNetworkName = "Urban Traffic Network (6 Nodes)";
                currentSource = 0;
                currentSink = 5;
                System.out.println("✔ Loaded Urban Traffic Network.");
            }
            case "6" -> {
                currentNetwork = SampleNetworks.createPipelineDistributionNetwork();
                currentNetworkName = "Water Pipeline Distribution System";
                currentSource = 0;
                currentSink = 6;
                System.out.println("✔ Loaded Water Pipeline System.");
            }
            default -> System.out.println("Invalid selection.");
        }
    }

    private void changeOriginDestinationPair() {
        try {
            System.out.printf("Current Vertices range: 0 to %d\n", currentNetwork.getNumVertices() - 1);
            for (int i = 0; i < currentNetwork.getNumVertices(); i++) {
                System.out.printf("  [%d] %s%s", i, currentNetwork.getVertexName(i), (i + 1) % 4 == 0 ? "\n" : "\t");
            }
            System.out.println();

            System.out.printf("Enter Source (Origin) index [%d]: ", currentSource);
            String sStr = scanner.nextLine().trim();
            int s = sStr.isEmpty() ? currentSource : Integer.parseInt(sStr);

            System.out.printf("Enter Sink (Destination) index [%d]: ", currentSink);
            String tStr = scanner.nextLine().trim();
            int t = tStr.isEmpty() ? currentSink : Integer.parseInt(tStr);

            if (s < 0 || s >= currentNetwork.getNumVertices() || t < 0 || t >= currentNetwork.getNumVertices() || s == t) {
                System.out.println("Invalid Source/Sink indices. Must be distinct valid vertices.");
                return;
            }

            currentSource = s;
            currentSink = t;
            System.out.printf("✔ Updated endpoints: %s -> %s\n",
                    currentNetwork.getVertexName(s), currentNetwork.getVertexName(t));
        } catch (Exception e) {
            System.out.println("Error changing endpoints: " + e.getMessage());
        }
    }

    private void runComparativeBenchmark() {
        System.out.println("\nRunning benchmark across all three algorithms on: " + currentNetworkName);
        List<BenchmarkResult> results = BenchmarkRunner.runComparison(currentNetwork, currentSource, currentSink);
        BenchmarkRunner.printComparisonTable(currentNetworkName, results);
    }

    private void runSingleAlgorithmDetailed() {
        System.out.println("\n--- Select Algorithm ---");
        System.out.println("1. Ford-Fulkerson Algorithm (DFS)");
        System.out.println("2. Edmonds-Karp Algorithm (BFS)");
        System.out.println("3. Dinic's Algorithm (Level Graph + Blocking Flow)");
        System.out.print("Choose (1-3): ");
        String sel = scanner.nextLine().trim();

        MaxFlowAlgorithm algo = switch (sel) {
            case "1" -> new FordFulkerson();
            case "2" -> new EdmondsKarp();
            case "3" -> new Dinic();
            default -> null;
        };

        if (algo == null) {
            System.out.println("Invalid choice.");
            return;
        }

        FlowNetwork netCopy = currentNetwork.cloneNetwork();
        FlowResult result = algo.computeMaxFlow(netCopy, currentSource, currentSink);

        System.out.println("\n" + result.getFormattedSummary(netCopy));
        System.out.println("Augmenting Paths Record (First 15 steps):");
        List<String> paths = result.getAugmentingPaths();
        int limit = Math.min(15, paths.size());
        for (int i = 0; i < limit; i++) {
            System.out.println("  " + paths.get(i));
        }
        if (paths.size() > 15) {
            System.out.printf("  ... (%d more augmentation steps)\n", paths.size() - 15);
        }
    }

    private void displayMinCutBottlenecks() {
        FlowNetwork netCopy = currentNetwork.cloneNetwork();
        Dinic dinic = new Dinic();
        FlowResult result = dinic.computeMaxFlow(netCopy, currentSource, currentSink);

        System.out.println("\n" + "=".repeat(60));
        System.out.println(" MIN-CUT & BOTTLENECK ANALYSIS: " + currentNetworkName);
        System.out.println("=".repeat(60));
        System.out.printf("Maximum Flow Throughput: %.2f\n", result.getMaxFlow());

        System.out.print("Source Cut Partition (S): [ ");
        for (int u : result.getSourceCutSet()) {
            System.out.print(netCopy.getVertexName(u) + " ");
        }
        System.out.println("]");

        System.out.print("Sink Cut Partition (T):   [ ");
        for (int v : result.getSinkCutSet()) {
            System.out.print(netCopy.getVertexName(v) + " ");
        }
        System.out.println("]");

        System.out.println("\nCritical Bottleneck Edges (Saturated S -> T Edges):");
        double totalCutCap = 0;
        for (FlowEdge edge : result.getMinCutEdges()) {
            System.out.printf("  🔴 Bottleneck Link: [%s -> %s] Capacity: %.2f | Flow: %.2f\n",
                    netCopy.getVertexName(edge.getFrom()),
                    netCopy.getVertexName(edge.getTo()),
                    edge.getCapacity(),
                    edge.getFlow());
            totalCutCap += edge.getCapacity();
        }
        System.out.printf("Total Minimum Cut Capacity: %.2f\n", totalCutCap);
        System.out.println("Key Insight: Expanding capacity on these specific bottleneck links will maximize system-wide traffic throughput!");
    }

    private void loadNetworkFromFile() {
        System.out.print("Enter file path (e.g. data/SiouxFalls_net.tntp or data/benchmark_12nodes.txt): ");
        String path = scanner.nextLine().trim();
        File file = new File(path);
        if (!file.exists()) {
            file = new File("data/" + path);
        }
        if (!file.exists()) {
            file = new File("C:/KL/2-1/DSA-3/" + path);
        }
        if (!file.exists()) {
            System.out.println("Error: File not found.");
            return;
        }

        try {
            FlowNetwork loaded;
            if (file.getName().endsWith(".tntp")) {
                loaded = TNTPNetworkLoader.loadSiouxFalls().network;
            } else {
                loaded = NetworkLoader.loadFromFile(file);
            }
            currentNetwork = loaded;
            currentNetworkName = file.getName();
            currentSource = 0;
            currentSink = loaded.getNumVertices() - 1;
            System.out.printf("✔ Successfully loaded '%s' with %d vertices and %d edges.\n",
                    file.getName(), loaded.getNumVertices(), loaded.getNumEdges());
        } catch (Exception e) {
            System.out.println("Failed to parse file: " + e.getMessage());
        }
    }

    private void createCustomNetworkInteractively() {
        try {
            System.out.print("Enter number of vertices: ");
            int n = Integer.parseInt(scanner.nextLine().trim());
            if (n < 2) {
                System.out.println("A network must have at least 2 vertices (source and sink).");
                return;
            }

            FlowNetwork net = new FlowNetwork(n);
            System.out.println("Enter name for each vertex (or press Enter for default):");
            for (int i = 0; i < n; i++) {
                System.out.printf("Vertex %d name [%s]: ", i, net.getVertexName(i));
                String name = scanner.nextLine().trim();
                if (!name.isEmpty()) {
                    net.setVertexName(i, name);
                }
            }

            System.out.print("Enter number of directed edges: ");
            int m = Integer.parseInt(scanner.nextLine().trim());
            System.out.println("Enter edges in format: <from_index> <to_index> <capacity>");
            for (int i = 0; i < m; i++) {
                System.out.printf("Edge #%d: ", i + 1);
                String[] parts = scanner.nextLine().trim().split("\\s+");
                int u = Integer.parseInt(parts[0]);
                int v = Integer.parseInt(parts[1]);
                double cap = Double.parseDouble(parts[2]);
                net.addEdge(u, v, cap);
            }

            System.out.print("Enter Source vertex index [0]: ");
            String sStr = scanner.nextLine().trim();
            int s = sStr.isEmpty() ? 0 : Integer.parseInt(sStr);

            System.out.printf("Enter Sink vertex index [%d]: ", n - 1);
            String tStr = scanner.nextLine().trim();
            int t = tStr.isEmpty() ? (n - 1) : Integer.parseInt(tStr);

            currentNetwork = net;
            currentNetworkName = "Custom User Network (" + n + " nodes)";
            currentSource = s;
            currentSink = t;
            System.out.println("✔ Custom network successfully configured!");
        } catch (Exception e) {
            System.out.println("Error creating custom network: " + e.getMessage());
        }
    }

    private void launchGUI() {
        System.out.println("Launching Swing Graphical Visualizer window with Sioux Falls geospatial map layout...");
        javax.swing.SwingUtilities.invokeLater(() -> {
            NetworkFlowVisualizerGUI gui = new NetworkFlowVisualizerGUI(currentNetwork, currentSource, currentSink);
            gui.setVisible(true);
        });
    }

    private void displayCurrentNetworkInfo() {
        System.out.println("\n" + currentNetwork.toString());
    }
}
