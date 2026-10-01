package algorithms;

import model.FlowEdge;
import model.FlowNetwork;
import model.FlowResult;

/**
 * Ford-Fulkerson Algorithm using Depth-First Search (DFS) to find augmenting paths.
 * Time Complexity: O(E * max_flow)
 */
public class FordFulkerson implements MaxFlowAlgorithm {
    private boolean[] marked;
    private FlowEdge[] edgeTo;

    @Override
    public String getAlgorithmName() {
        return "Ford-Fulkerson (DFS)";
    }

    @Override
    public String getTimeComplexity() {
        return "O(E * max_flow)";
    }

    @Override
    public FlowResult computeMaxFlow(FlowNetwork network, int source, int sink) {
        long startTime = System.nanoTime();
        network.resetFlows();

        FlowResult result = new FlowResult(getAlgorithmName(), getTimeComplexity());
        double maxFlow = 0.0;
        int iterations = 0;

        while (hasAugmentingPathDFS(network, source, sink)) {
            // Find bottleneck capacity along the augmenting path
            double bottle = Double.POSITIVE_INFINITY;
            for (int v = sink; v != source; v = edgeTo[v].other(v)) {
                bottle = Math.min(bottle, edgeTo[v].residualCapacityTo(v));
            }

            // Augment flow along the path
            StringBuilder pathTrace = new StringBuilder();
            for (int v = sink; v != source; v = edgeTo[v].other(v)) {
                edgeTo[v].addResidualFlowTo(v, bottle);
            }

            maxFlow += bottle;
            iterations++;

            // Record path trace for inspection
            recordPath(network, source, sink, bottle, iterations, result);
        }

        long endTime = System.nanoTime();
        result.setMaxFlow(maxFlow);
        result.setIterations(iterations);
        result.setExecutionTimeNanos(endTime - startTime);

        // Compute Min-Cut bottlenecks
        MinCutAnalyzer.MinCutResult minCut = MinCutAnalyzer.findMinCut(network, source);
        result.setSourceCutSet(minCut.getSourceSetS());
        result.setSinkCutSet(minCut.getSinkSetT());
        result.setMinCutEdges(minCut.getCutEdges());

        return result;
    }

    private boolean hasAugmentingPathDFS(FlowNetwork network, int source, int sink) {
        marked = new boolean[network.getNumVertices()];
        edgeTo = new FlowEdge[network.getNumVertices()];
        return dfs(network, source, sink);
    }

    private boolean dfs(FlowNetwork network, int u, int sink) {
        marked[u] = true;
        if (u == sink) return true;

        for (FlowEdge edge : network.adj(u)) {
            int v = edge.other(u);
            if (!marked[v] && edge.residualCapacityTo(v) > 1e-9) {
                edgeTo[v] = edge;
                if (dfs(network, v, sink)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void recordPath(FlowNetwork network, int source, int sink, double bottle, int iter, FlowResult result) {
        java.util.List<String> pathNodes = new java.util.ArrayList<>();
        for (int v = sink; v != source; v = edgeTo[v].other(v)) {
            pathNodes.add(network.getVertexName(v));
        }
        pathNodes.add(network.getVertexName(source));
        java.util.Collections.reverse(pathNodes);
        result.addAugmentingPath(String.format("Iter %d: Path [%s] -> Bottleneck Added: %.2f",
                iter, String.join(" -> ", pathNodes), bottle));
    }
}
