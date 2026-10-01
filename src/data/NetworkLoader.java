package data;

import model.FlowEdge;
import model.FlowNetwork;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading and writing flow networks to and from plain text files.
 * Format:
 * # Nodes: index name
 * 0 Source
 * 1 Intermediate
 * 2 Sink
 * # Edges: from to capacity
 * 0 1 10.0
 * 1 2 8.0
 */
public class NetworkLoader {

    public static FlowNetwork loadFromFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<String[]> nodeLines = new ArrayList<>();
            List<String[]> edgeLines = new ArrayList<>();
            boolean readingEdges = false;

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    if (line.toLowerCase().contains("edges")) {
                        readingEdges = true;
                    }
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (readingEdges) {
                    edgeLines.add(parts);
                } else {
                    nodeLines.add(parts);
                }
            }

            int n = nodeLines.size();
            if (n == 0) {
                // If no node headers were defined, deduce n from max vertex ID in edges
                int maxV = 0;
                for (String[] parts : edgeLines) {
                    maxV = Math.max(maxV, Math.max(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
                }
                n = maxV + 1;
            }

            FlowNetwork network = new FlowNetwork(n);

            for (String[] parts : nodeLines) {
                int id = Integer.parseInt(parts[0]);
                String name = parts.length > 1 ? parts[1] : ("V" + id);
                network.setVertexName(id, name);
            }

            for (String[] parts : edgeLines) {
                int u = Integer.parseInt(parts[0]);
                int v = Integer.parseInt(parts[1]);
                double cap = Double.parseDouble(parts[2]);
                network.addEdge(u, v, cap);
            }

            return network;
        }
    }

    public static void saveToFile(FlowNetwork network, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("# Nodes: <id> <name>");
            for (int i = 0; i < network.getNumVertices(); i++) {
                writer.printf("%d %s\n", i, network.getVertexName(i).replaceAll("\\s+", "_"));
            }
            writer.println();
            writer.println("# Edges: <from> <to> <capacity>");
            for (FlowEdge edge : network.getForwardEdges()) {
                writer.printf("%d %d %.2f\n", edge.getFrom(), edge.getTo(), edge.getCapacity());
            }
        }
    }
}
