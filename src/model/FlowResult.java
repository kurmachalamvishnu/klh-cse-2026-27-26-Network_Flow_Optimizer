package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Encapsulates the results of a network flow algorithm execution,
 * including flow value, computational performance metrics, and augmenting paths.
 */
public class FlowResult {
    private final String algorithmName;
    private final String timeComplexity;
    private double maxFlow;
    private int iterations;
    private long executionTimeNanos;
    private final List<String> augmentingPaths;
    private Set<Integer> sourceCutSet;
    private Set<Integer> sinkCutSet;
    private List<FlowEdge> minCutEdges;

    public FlowResult(String algorithmName, String timeComplexity) {
        this.algorithmName = algorithmName;
        this.timeComplexity = timeComplexity;
        this.augmentingPaths = new ArrayList<>();
        this.minCutEdges = new ArrayList<>();
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public String getTimeComplexity() {
        return timeComplexity;
    }

    public double getMaxFlow() {
        return maxFlow;
    }

    public void setMaxFlow(double maxFlow) {
        this.maxFlow = maxFlow;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public long getExecutionTimeNanos() {
        return executionTimeNanos;
    }

    public void setExecutionTimeNanos(long executionTimeNanos) {
        this.executionTimeNanos = executionTimeNanos;
    }

    public double getExecutionTimeMs() {
        return executionTimeNanos / 1_000_000.0;
    }

    public List<String> getAugmentingPaths() {
        return augmentingPaths;
    }

    public void addAugmentingPath(String pathDescription) {
        this.augmentingPaths.add(pathDescription);
    }

    public Set<Integer> getSourceCutSet() {
        return sourceCutSet;
    }

    public void setSourceCutSet(Set<Integer> sourceCutSet) {
        this.sourceCutSet = sourceCutSet;
    }

    public Set<Integer> getSinkCutSet() {
        return sinkCutSet;
    }

    public void setSinkCutSet(Set<Integer> sinkCutSet) {
        this.sinkCutSet = sinkCutSet;
    }

    public List<FlowEdge> getMinCutEdges() {
        return minCutEdges;
    }

    public void setMinCutEdges(List<FlowEdge> minCutEdges) {
        this.minCutEdges = minCutEdges;
    }

    public String getFormattedSummary(FlowNetwork network) {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append(String.format("Algorithm:           %s\n", algorithmName));
        sb.append(String.format("Theoretical Bound:   %s\n", timeComplexity));
        sb.append(String.format("Computed Max Flow:   %.2f\n", maxFlow));
        sb.append(String.format("Augmentations/Iter:  %d\n", iterations));
        sb.append(String.format("Execution Time:      %.3f ms (%d µs)\n", getExecutionTimeMs(), executionTimeNanos / 1000));
        sb.append("====================================================\n");

        if (minCutEdges != null && !minCutEdges.isEmpty()) {
            double cutCapacity = minCutEdges.stream().mapToDouble(FlowEdge::getCapacity).sum();
            sb.append(String.format("Minimum Cut Bottlenecks (Total Cut Capacity = %.2f):\n", cutCapacity));
            for (FlowEdge edge : minCutEdges) {
                sb.append(String.format("  * [%s -> %s] (Capacity: %.2f, Saturated Flow: %.2f)\n",
                        network.getVertexName(edge.getFrom()),
                        network.getVertexName(edge.getTo()),
                        edge.getCapacity(),
                        edge.getFlow()));
            }
        }
        return sb.toString();
    }
}
