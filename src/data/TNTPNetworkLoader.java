package data;

import model.FlowNetwork;

import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Specialized parser for Transportation Networks for Research (TNTP) dataset files:
 * 1. SiouxFalls_net.tntp  - Directed links, physical road capacities, lengths
 * 2. SiouxFalls_node.tntp - Geospatial (X, Y) coordinates for visual layout
 * 3. SiouxFalls_trips.tntp- Origin-Destination (OD) trip demand matrix
 */
public class TNTPNetworkLoader {

    public static class SiouxFallsData {
        public FlowNetwork network;
        public Map<Integer, Point2D.Double> coordinates;
        public double[][] odMatrix;
        public int numZones;
        public int numNodes;
        public int numLinks;

        public SiouxFallsData(FlowNetwork network, Map<Integer, Point2D.Double> coordinates,
                              double[][] odMatrix, int numZones, int numNodes, int numLinks) {
            this.network = network;
            this.coordinates = coordinates;
            this.odMatrix = odMatrix;
            this.numZones = numZones;
            this.numNodes = numNodes;
            this.numLinks = numLinks;
        }

        /**
         * Creates a System-Wide Super-Source & Super-Sink network based on the full OD demand matrix.
         * Super-Source (index 24) -> Origins (capacities = total trip supply)
         * Internal 76 road links (capacities = physical vehicle capacities)
         * Destinations -> Super-Sink (index 25) (capacities = total trip attraction)
         */
        public FlowNetwork buildSystemWideFlowNetwork() {
            int n = numNodes;
            int superSource = n;      // index 24
            int superSink = n + 1;    // index 25

            FlowNetwork sysNet = new FlowNetwork(n + 2);

            // Copy node names
            for (int i = 0; i < n; i++) {
                sysNet.setVertexName(i, network.getVertexName(i));
            }
            sysNet.setVertexName(superSource, "Super_Source (S*)");
            sysNet.setVertexName(superSink, "Super_Sink (T*)");

            // Copy all 76 physical road links
            for (model.FlowEdge edge : network.getForwardEdges()) {
                sysNet.addEdge(edge.getFrom(), edge.getTo(), edge.getCapacity());
            }

            // Compute total origin outflows and destination inflows
            double[] originDemand = new double[n];
            double[] destAttraction = new double[n];

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    originDemand[i] += odMatrix[i][j];
                    destAttraction[j] += odMatrix[i][j];
                }
            }

            // Connect Super-Source to origins
            for (int i = 0; i < n; i++) {
                if (originDemand[i] > 0) {
                    sysNet.addEdge(superSource, i, originDemand[i]);
                }
            }

            // Connect destinations to Super-Sink
            for (int j = 0; j < n; j++) {
                if (destAttraction[j] > 0) {
                    sysNet.addEdge(j, superSink, destAttraction[j]);
                }
            }

            return sysNet;
        }
    }

    /**
     * Resolves the file path by checking both local project data/ and C:\KL\2-1\DSA-3\
     */
    public static File resolveFile(String fileName) {
        File f1 = new File("data/" + fileName);
        if (f1.exists()) return f1;

        File f2 = new File("C:/KL/2-1/DSA-3/" + fileName);
        if (f2.exists()) return f2;

        File f3 = new File(fileName);
        if (f3.exists()) return f3;

        throw new RuntimeException("TNTP file not found: " + fileName + ". Checked data/ and C:/KL/2-1/DSA-3/");
    }

    /**
     * Loads the complete Sioux Falls dataset suite.
     */
    public static SiouxFallsData loadSiouxFalls() throws IOException {
        File netFile = resolveFile("SiouxFalls_net.tntp");
        File nodeFile = resolveFile("SiouxFalls_node.tntp");
        File tripsFile = resolveFile("SiouxFalls_trips.tntp");

        Map<Integer, Point2D.Double> coords = loadCoordinates(nodeFile);
        int numNodes = coords.isEmpty() ? 24 : coords.size();

        FlowNetwork net = new FlowNetwork(numNodes);
        for (int i = 0; i < numNodes; i++) {
            net.setVertexName(i, "Zone_" + (i + 1));
        }

        int linksCount = loadNetworkLinks(netFile, net);
        double[][] odMatrix = loadTripsMatrix(tripsFile, numNodes);

        return new SiouxFallsData(net, coords, odMatrix, numNodes, numNodes, linksCount);
    }

    private static int loadNetworkLinks(File file, FlowNetwork network) throws IOException {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("<") || line.startsWith("~")) {
                    continue;
                }

                String[] tokens = line.split("\\s+");
                if (tokens.length >= 3) {
                    try {
                        int u = Integer.parseInt(tokens[0]) - 1; // 1-based to 0-based
                        int v = Integer.parseInt(tokens[1]) - 1;
                        double capacity = Double.parseDouble(tokens[2]);

                        network.addEdge(u, v, capacity);
                        count++;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return count;
    }

    public static Map<Integer, Point2D.Double> loadCoordinates(File file) throws IOException {
        Map<Integer, Point2D.Double> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("Node") || line.startsWith(";")) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    try {
                        int nodeId = Integer.parseInt(parts[0]) - 1; // 1-based to 0-based
                        double x = Double.parseDouble(parts[1]);
                        double y = Double.parseDouble(parts[2]);
                        map.put(nodeId, new Point2D.Double(x, y));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return map;
    }

    public static double[][] loadTripsMatrix(File file, int numNodes) throws IOException {
        double[][] matrix = new double[numNodes][numNodes];
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("<")) continue;
                sb.append(line).append("\n");
            }

            String content = sb.toString();
            // Match Origin blocks: Origin <num>
            Pattern originPattern = Pattern.compile("Origin\\s+(\\d+)(.*?)(?=Origin|\\Z)", Pattern.DOTALL);
            Matcher originMatcher = originPattern.matcher(content);

            Pattern pairPattern = Pattern.compile("(\\d+)\\s*:\\s*([\\d\\.]+)\\s*;");

            while (originMatcher.find()) {
                int origin = Integer.parseInt(originMatcher.group(1)) - 1;
                String block = originMatcher.group(2);

                Matcher pairMatcher = pairPattern.matcher(block);
                while (pairMatcher.find()) {
                    int dest = Integer.parseInt(pairMatcher.group(1)) - 1;
                    double flow = Double.parseDouble(pairMatcher.group(2));
                    if (origin >= 0 && origin < numNodes && dest >= 0 && dest < numNodes) {
                        matrix[origin][dest] = flow;
                    }
                }
            }
        }
        return matrix;
    }
}
