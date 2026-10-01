package algorithms;

import model.FlowEdge;
import model.FlowNetwork;
import model.FlowResult;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * Edmonds-Karp Algorithm using Breadth-First Search (BFS) to find shortest augmenting paths.
 * Time Complexity: O(V * E^2)
 */
public class EdmondsKarp implements MaxFlowAlgorithm {
    private boolean[] marked;
    private FlowEdge[] edgeTo;

    @Override
    public String getAlgorithmName() {
        return "Edmonds-Karp (BFS)";
    }

    @Override
    public String getTimeComplexity() {
        return "O(V * E^2)";
    }

    @Override
    public FlowResult computeMaxFlow(FlowNetwork network, int source, int sink) {
        long startTime = System.nanoTime();
        network.resetFlows();

        FlowResult result = new FlowResult(getAlgorithmName(), getTimeComplexity());
        double maxFlow = 0.0;
        int iterations = 0;

        while (hasAugmentingPathBFS(network, source, sink)) {
            // Find bottleneck capacity along the augmenting path
            double bottle = Double.POSITIVE_INFINITY;
            for (int v = sink; v != source; v = edgeTo[v].other(v)) {
                bottle = Math.min(bottle, edgeTo[v].residualCapacityTo(v));
            }

            // Augment flow along the path
            for (int v = sink; v != source; v = edgeTo[v].other(v)) {
                edgeTo[v].addResidualFlowTo(v, bottle);
            }

            maxFlow += bottle;
            iterations++;

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

    private boolean hasAugmentingPathBFS(FlowNetwork network, int source, int sink) {
        marked = new boolean[network.getNumVertices()];
        edgeTo = new FlowEdge[network.getNumVertices()];
        Queue<Integer> queue = new ArrayDeque<>();

        marked[source] = true;
        queue.add(source);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            if (u == sink) break;

            for (FlowEdge edge : network.adj(u)) {
                int v = edge.other(u);
                if (!marked[v] && edge.residualCapacityTo(v) > 1e-9) {
                    edgeTo[v] = edge;
                    marked[v] = true;
                    queue.add(v);
                }
            }
        }
        return marked[sink];
    }

    private void recordPath(FlowNetwork network, int source, int sink, double bottle, int iter, FlowResult result) {
        List<String> pathNodes = new ArrayList<>();
        for (int v = sink; v != source; v = edgeTo[v].other(v)) {
            pathNodes.add(network.getVertexName(v));
        }
        pathNodes.add(network.getVertexName(source));
        Collections.reverse(pathNodes);
        result.addAugmentingPath(String.format("Iter %d: Path [%s] -> Bottleneck Added: %.2f",
                iter, String.join(" -> ", pathNodes), bottle));
    }
}
