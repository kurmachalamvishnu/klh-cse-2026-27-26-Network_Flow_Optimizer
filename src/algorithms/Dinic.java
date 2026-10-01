package algorithms;

import model.FlowEdge;
import model.FlowNetwork;
import model.FlowResult;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * Dinic's Algorithm using Level Graphs (BFS) and Blocking Flows (DFS with pointer optimization).
 * Time Complexity: O(V^2 * E)
 */
public class Dinic implements MaxFlowAlgorithm {
    private int[] level;
    private int[] nextEdgePtr;

    @Override
    public String getAlgorithmName() {
        return "Dinic's Algorithm";
    }

    @Override
    public String getTimeComplexity() {
        return "O(V^2 * E)";
    }

    @Override
    public FlowResult computeMaxFlow(FlowNetwork network, int source, int sink) {
        long startTime = System.nanoTime();
        network.resetFlows();

        FlowResult result = new FlowResult(getAlgorithmName(), getTimeComplexity());
        int n = network.getNumVertices();
        level = new int[n];
        nextEdgePtr = new int[n];

        double maxFlow = 0.0;
        int phases = 0;
        int totalPushedPaths = 0;

        // While sink is reachable in the level graph
        while (bfsLevelGraph(network, source, sink)) {
            phases++;
            Arrays.fill(nextEdgePtr, 0);

            double pushed;
            while ((pushed = dfsSendFlow(network, source, sink, Double.POSITIVE_INFINITY)) > 1e-9) {
                maxFlow += pushed;
                totalPushedPaths++;
                result.addAugmentingPath(String.format("Phase %d (Augmentation #%d): Pushed Flow = %.2f",
                        phases, totalPushedPaths, pushed));
            }
        }

        long endTime = System.nanoTime();
        result.setMaxFlow(maxFlow);
        // Reporting phases as Dinic's iterations (and path count in summary)
        result.setIterations(phases);
        result.setExecutionTimeNanos(endTime - startTime);

        // Compute Min-Cut bottlenecks
        MinCutAnalyzer.MinCutResult minCut = MinCutAnalyzer.findMinCut(network, source);
        result.setSourceCutSet(minCut.getSourceSetS());
        result.setSinkCutSet(minCut.getSinkSetT());
        result.setMinCutEdges(minCut.getCutEdges());

        return result;
    }

    /**
     * Builds level graph using BFS. Returns true if sink is reachable from source.
     */
    private boolean bfsLevelGraph(FlowNetwork network, int source, int sink) {
        Arrays.fill(level, -1);
        level[source] = 0;

        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (FlowEdge edge : network.adj(u)) {
                int v = edge.other(u);
                if (level[v] < 0 && edge.residualCapacityTo(v) > 1e-9) {
                    level[v] = level[u] + 1;
                    queue.add(v);
                }
            }
        }
        return level[sink] >= 0;
    }

    /**
     * Pushes blocking flow along level graph using DFS with pointer optimization.
     */
    private double dfsSendFlow(FlowNetwork network, int u, int sink, double currentFlow) {
        if (u == sink) return currentFlow;

        java.util.List<FlowEdge> edges = network.adj(u);
        for (; nextEdgePtr[u] < edges.size(); nextEdgePtr[u]++) {
            FlowEdge edge = edges.get(nextEdgePtr[u]);
            int v = edge.other(u);

            if (level[v] == level[u] + 1 && edge.residualCapacityTo(v) > 1e-9) {
                double bottle = Math.min(currentFlow, edge.residualCapacityTo(v));
                double tr = dfsSendFlow(network, v, sink, bottle);

                if (tr > 1e-9) {
                    edge.addResidualFlowTo(v, tr);
                    return tr;
                }
            }
        }
        return 0.0;
    }
}
