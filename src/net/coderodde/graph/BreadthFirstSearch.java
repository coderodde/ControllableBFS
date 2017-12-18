package net.coderodde.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class implements the breadth-first search.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Dec 18, 2017)
 * @param <N> the graph node type.
 */
public final class BreadthFirstSearch<N extends ChildNodeExpander<N>> {

    /**
     * This interface implements the node listener.
     * 
     * @param <N> the graph node type.
     */
    public interface NodeListener<N> {
        
        /**
         * Called right before starting to expand the nodes.
         * 
         * @param sourceNode the source node.
         */
        public void onBeginSearch(N sourceNode);
        
        /**
         * Called when {@code node} is reached by the search for the first time.
         * 
         * @param node the reached node.
         */
        public void onReach(N node);
        
        /**
         * Called when {@code node} is removed from the search frontier and is
         * expanded.
         * 
         * @param node the expanded node.
         */
        public void onExpand(N node);
        
        /**
         * Called right after the graph search reaches the target node.
         * 
         * @param targetNode the target node.
         */
        public void onEndSearchSuccess(N targetNode);
        
        /**
         * Called right after the graph search decides that the target node is
         * not reachable.
         */
        public void onEndSearchFailure();
    }
    
    /**
     * Initiates the path search.
     * 
     * @param <N> the graph node type.
     * @return the source node selector.
     */
    public static <N extends ChildNodeExpander<N>> 
            SourceNodeSelector<N> findShortestPath() {
        return new SourceNodeSelector<>();
    }

    /**
     * Implements the source node selector.
     * 
     * @param <N> the graph node type.
     */
    public static final class 
            SourceNodeSelector<N extends ChildNodeExpander<N>> {

        /**
         * Selects the source node.
         * 
         * @param sourceNode the source node.
         * @return the target node selector.
         */
        TargetNodeSelector<N> from(N sourceNode) {
            return new TargetNodeSelector<>(
                Objects.requireNonNull(sourceNode, "The sourceNode is null."));
        }
    }

    /**
     * Implements the target node selector.
     * 
     * @param <N> the graph node type.
     */
    public static final class 
            TargetNodeSelector<N extends ChildNodeExpander<N>> {

        private final N sourceNode;

        TargetNodeSelector(N sourceNode) {
            this.sourceNode = sourceNode;
        }

        /**
         * Selects the target node.
         * 
         * @param targetNode the target node.
         * @return the search state.
         */
        public SearchState<N> to(N targetNode) {
            return new SearchState<>(
                sourceNode,
                Objects.requireNonNull(targetNode, "The targetNode is null."));
        }
    }

    /**
     * Implements the actual path search state.
     * 
     * @param <N> the graph node.
     */
    public static final class SearchState<N extends ChildNodeExpander<N>> {

        /**
         * The state of search.
         */
        public static enum State {
            RUNNING,    // Still running.
            FOUND_PATH, // Target node reached.
            NO_PATH     // Target node not reachable.
        }

        private static final int DEFAULT_NUMBER_OF_EXPANSIONS_PER_ITERATION = 1;

        private final N sourceNode;
        private final N targetNode;
        private int expansionsPerIteration = 
                DEFAULT_NUMBER_OF_EXPANSIONS_PER_ITERATION;

        private State state = null;
        private final Deque<N> queue = new ArrayDeque<>();
        private final Map<N, N> parents = new HashMap<>();
        private final List<NodeListener<N>> nodeListeners = new ArrayList<>();

        SearchState(N sourceNode, N targetNode) {
            this.sourceNode = sourceNode;
            this.targetNode = targetNode;
            this.queue.addLast(sourceNode);
            this.parents.put(sourceNode, null);
        }

        /**
         * Sets the number of node expansion per single iteration, i.e., per 
         * single call to {@link iterate}.
         * 
         * @param expansions the number of expansions per iteration.
         * @return this state object.
         */
        public SearchState<N> setExpansionsPerIteration(int expansions) {
            this.expansionsPerIteration = Math.max(1, expansions);
            return this;
        }
        
        public SearchState<N> addNodeListener(NodeListener<N> listener) {
            nodeListeners.add(
                    Objects.requireNonNull(
                            listener, 
                            "The input listener is null."));
            return this;
        }

        /**
         * Performs a single iteration.
         */
        public void iterate() {
            if (state == null) {
                onBeginSearch(sourceNode);
                state = State.RUNNING;
            }
            
            if (isSearchComplete()) {
                return;
            }

            for (int i = 0; i < expansionsPerIteration; ++i) {
                if (queue.isEmpty()) {
                    state = State.NO_PATH;
                    onEndSearchFailure(targetNode);
                    return;
                }

                N currentNode = queue.removeFirst();
                onExpand(currentNode);
                
                if (currentNode.equals(targetNode)) {
                    state = State.FOUND_PATH;
                    onEndSearchSuccess(targetNode);
                    return;
                }

                for (N childNode : currentNode.getChildren()) {
                    if (!parents.containsKey(childNode)) {
                        // We came to childNode from currentNode:
                        onReach(childNode);
                        parents.put(childNode, currentNode); 
                        queue.addLast(childNode);
                    }
                }
            }
        }

        /**
         * Searches for the 
         */
        public void search() {
            while (!isSearchComplete()) {
                iterate();
            }
        }

        /**
         * Searches until {@code milliseconds} milliseconds are elapsed, or
         * until the search is complete, whichever happens first.
         * 
         * @param milliseconds the maximum allowed duration of the search.
         */
        public void searchFor(long milliseconds) {
            long startTime = System.currentTimeMillis();

            while (!isSearchComplete()) {
                iterate();
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;

                if (elapsedTime >= milliseconds) {
                    break;
                }
            }
        }

        public State getState() {
            return state;
        }

        public boolean isSearchComplete() {
            return state != null && state != State.RUNNING;
        }

        public List<N> getShortestPath() {
            switch (state) {
                case RUNNING:
                    throw new IllegalStateException(
                            "The path search is not yet complete.");

                case NO_PATH:
                    throw new IllegalStateException(
                            "The target node is not reachable from the source " +
                                    "node.");

                case FOUND_PATH:
                    return tracebackPath();

                default:
                    throw new IllegalStateException("This should not happen.");
            }
        }

        private List<N> tracebackPath() {
            List<N> path = new ArrayList<>();
            N currentNode = targetNode;

            while (currentNode != null) {
                path.add(currentNode);
                currentNode = parents.get(currentNode);
            }

            Collections.<N>reverse(path);
            return path;
        }
        
        private void onBeginSearch(N sourceNode) {
            for (NodeListener<N> nodeListener : nodeListeners) {
                nodeListener.onBeginSearch(sourceNode);
            }
        }
        
        private void onReach(N node) {
            for (NodeListener<N> nodeListener : nodeListeners) {
                nodeListener.onReach(node);
            }
        }
        
        private void onExpand(N node) {
            for (NodeListener<N> nodeListener : nodeListeners) {
                nodeListener.onExpand(node);
            }
        }
        
        private void onEndSearchSuccess(N targetNode) {
            for (NodeListener<N> nodeListener : nodeListeners) {
                nodeListener.onEndSearchSuccess(targetNode);
            }
        }
        
        private void onEndSearchFailure(N targetNode) {
            for (NodeListener<N> nodeListener : nodeListeners) {
                nodeListener.onEndSearchFailure();
            }
        }
    }
}
