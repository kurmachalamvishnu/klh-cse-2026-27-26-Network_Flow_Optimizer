package benchmark;

import model.FlowResult;

/**
 * Stores benchmark metrics for an algorithm run on a specific network.
 */
public class BenchmarkResult {
    private final String algorithmName;
    private final String timeComplexity;
    private final double maxFlow;
    private final int iterations;
    private final double executionTimeMs;
    private final long executionTimeMicros;
    private final int bottleneckEdgesCount;
    private final double minCutCapacity;

    public BenchmarkResult(FlowResult result) {
        this.algorithmName = result.getAlgorithmName();
        this.timeComplexity = result.getTimeComplexity();
        this.maxFlow = result.getMaxFlow();
        this.iterations = result.getIterations();
        this.executionTimeMs = result.getExecutionTimeMs();
        this.executionTimeMicros = result.getExecutionTimeNanos() / 1000;
        this.bottleneckEdgesCount = result.getMinCutEdges() != null ? result.getMinCutEdges().size() : 0;
        this.minCutCapacity = result.getMinCutEdges() != null
                ? result.getMinCutEdges().stream().mapToDouble(e -> e.getCapacity()).sum()
                : 0.0;
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

    public int getIterations() {
        return iterations;
    }

    public double getExecutionTimeMs() {
        return executionTimeMs;
    }

    public long getExecutionTimeMicros() {
        return executionTimeMicros;
    }

    public int getBottleneckEdgesCount() {
        return bottleneckEdgesCount;
    }

    public double getMinCutCapacity() {
        return minCutCapacity;
    }
}
