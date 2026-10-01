package algorithms;

import model.FlowNetwork;
import model.FlowResult;

/**
 * Standard interface for all Maximum Flow algorithms.
 */
public interface MaxFlowAlgorithm {
    /**
     * Solves the maximum flow problem on the given network from source to sink.
     * Note: Network flows will be updated to reflect the optimal flow distribution.
     *
     * @param network the flow network
     * @param source  source vertex index
     * @param sink    sink vertex index
     * @return FlowResult containing computed max flow and execution metrics
     */
    FlowResult computeMaxFlow(FlowNetwork network, int source, int sink);

    /**
     * Returns the human-readable algorithm name.
     */
    String getAlgorithmName();

    /**
     * Returns the Big-O asymptotic time complexity.
     */
    String getTimeComplexity();
}
