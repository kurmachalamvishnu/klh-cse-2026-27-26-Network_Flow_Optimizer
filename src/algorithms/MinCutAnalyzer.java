package algorithms;

import model.FlowEdge;
import model.FlowNetwork;

import java.util.*;

/**
 * Analyzes the flow network to identify the Minimum S-T Cut based on the
 * Max-Flow Min-Cut Theorem. Identifies bottleneck edges restricting maximum capacity.
 */
public class MinCutAnalyzer {

    public static class MinCutResult {
        private final Set<Integer> sourceSetS;
        private final Set<Integer> sinkSetT;
        private final List<FlowEdge> cutEdges;
        private final double totalCutCapacity;

        public MinCutResult(Set<Integer> sourceSetS, Set<Integer> sinkSetT, List<FlowEdge> cutEdges, double totalCutCapacity) {
            this.sourceSetS = sourceSetS;
            this.sinkSetT = sinkSetT;
            this.cutEdges = cutEdges;
            this.totalCutCapacity = totalCutCapacity;
        }

        public Set<Integer> getSourceSetS() {
            return sourceSetS;
        }

        public Set<Integer> getSinkSetT() {
            return sinkSetT;
        }

        public List<FlowEdge> getCutEdges() {
            return cutEdges;
        }

        public double getTotalCutCapacity() {
            return totalCutCapacity;
        }
    }

    /**
     * Computes the Minimum Cut partition (S, T) and extracts the bottleneck cut edges.
     */
    public static MinCutResult findMinCut(FlowNetwork network, int source) {
        int n = network.getNumVertices();
        boolean[] visited = new boolean[n];
        Queue<Integer> queue = new ArrayDeque<>();

        // BFS traversal in the residual graph from source
        visited[source] = true;
        queue.add(source);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (FlowEdge edge : network.adj(u)) {
                int v = edge.other(u);
                if (!visited[v] && edge.residualCapacityTo(v) > 1e-9) {
                    visited[v] = true;
                    queue.add(v);
                }
            }
        }

        Set<Integer> sourceSetS = new HashSet<>();
        Set<Integer> sinkSetT = new HashSet<>();
        for (int i = 0; i < n; i++) {
            if (visited[i]) {
                sourceSetS.add(i);
            } else {
                sinkSetT.add(i);
            }
        }

        List<FlowEdge> cutEdges = new ArrayList<>();
        double totalCapacity = 0.0;

        // Forward edges crossing from S to T
        for (FlowEdge edge : network.getForwardEdges()) {
            if (sourceSetS.contains(edge.getFrom()) && sinkSetT.contains(edge.getTo())) {
                cutEdges.add(edge);
                totalCapacity += edge.getCapacity();
            }
        }

        return new MinCutResult(sourceSetS, sinkSetT, cutEdges, totalCapacity);
    }
}
