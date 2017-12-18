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

        private final N targetNode;
        private int expansionsPerIteration = 
                DEFAULT_NUMBER_OF_EXPANSIONS_PER_ITERATION;

        private State state = State.RUNNING;
        private final Deque<N> queue = new ArrayDeque<>();
        private final Map<N, N> parents = new HashMap<>();

        SearchState(N sourceNode, N targetNode) {
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

        /**
         * Performs a single iteration.
         */
        public void iterate() {
            if (isSearchComplete()) {
                return;
            }

            for (int i = 0; i < expansionsPerIteration; ++i) {
                if (queue.isEmpty()) {
                    state = State.NO_PATH;
                    return;
                }

                N currentNode = queue.removeFirst();

                if (currentNode.equals(targetNode)) {
                    state = State.FOUND_PATH;
                    return;
                }

                for (N childNode : currentNode.getChildren()) {
                    if (!parents.containsKey(childNode)) {
                        // We came to childNode from currentNode:
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
            return state != State.RUNNING;
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
    }
}
