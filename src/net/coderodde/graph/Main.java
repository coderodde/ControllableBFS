package net.coderodde.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Runs a demonstration program.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Dec 18, 2017)
 */
public final class Main {
    
    private static final int NODES = 1_000_000;
    private static final int ARCS = 6_000_000;
    
    public static void main(String[] args) {
        Random random = new Random();
        System.out.println("Building graph...");
        List<DirectedGraphNode> nodes = createRandomGraph(NODES,
                                                          ARCS,
                                                          random);
        
        System.out.println("Graph built!");
        DirectedGraphNode sourceNode = choose(nodes, random);
        DirectedGraphNode targetNode = choose(nodes, random);
        
        System.out.println();
        System.out.println("Source: " + sourceNode);
        System.out.println("Target: " + targetNode);
        System.out.println();
        
        long start = System.currentTimeMillis();
        
        BreadthFirstSearch.SearchState<DirectedGraphNode> bfs = 
                BreadthFirstSearch.<DirectedGraphNode>findShortestPath()
                                  .from(sourceNode)
                                  .to(targetNode)
                                  .setExpansionsPerIteration(10);
        
        while (!bfs.isSearchComplete()) {
            bfs.searchFor(250L);
            System.out.println("Time elapsed.");
        }
        
        long end = System.currentTimeMillis();
        
        if (bfs.getState()
                .equals(BreadthFirstSearch.SearchState.State.FOUND_PATH)) {
            System.out.println("Path: " + bfs.getShortestPath());
        } else {
            System.out.println("Target not reachable.");
        }
        
        System.out.println("Duration: " + (end - start) + " milliseconds.");
        System.out.println();
        
        start = System.currentTimeMillis();
        
        bfs = BreadthFirstSearch.<DirectedGraphNode>findShortestPath()
                                .from(sourceNode)
                                .to(targetNode)
                                .setExpansionsPerIteration(10);
        
        bfs.search();
        end = System.currentTimeMillis();
        
        if (bfs.getState()
                .equals(BreadthFirstSearch.SearchState.State.FOUND_PATH)) {
            System.out.println("Path: " + bfs.getShortestPath());
        } else {
            System.out.println("Target not reachable.");
        }
        
        System.out.println("Duration: " + (end - start) + " milliseconds.");
    }
    
    private static List<DirectedGraphNode> createRandomGraph(int nodes,
                                                             int arcs,
                                                             Random random) {
        List<DirectedGraphNode> nodeList = new ArrayList<>(nodes);
        
        for (int i = 0; i < nodes; ++i) {
            nodeList.add(new DirectedGraphNode(i));
        }
        
        for (int arc = 0; arc < arcs; ++arc) {
            DirectedGraphNode tail = choose(nodeList, random);
            DirectedGraphNode head = choose(nodeList, random);
            tail.addChild(head);
        }
        
        return nodeList;
    }
    
    private static <T> T choose(List<T> list, Random random) {
        return list.get(random.nextInt(list.size()));
    }
}
