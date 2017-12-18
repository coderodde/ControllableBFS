package net.coderodde.graph;

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BreadthFirstSearchTest {
    
    @Test
    public void test() {
        DirectedGraphNode a  = new DirectedGraphNode(0);
        DirectedGraphNode b  = new DirectedGraphNode(1);
        DirectedGraphNode c1 = new DirectedGraphNode(2);
        DirectedGraphNode c2 = new DirectedGraphNode(3);
        DirectedGraphNode d  = new DirectedGraphNode(4);
        DirectedGraphNode e  = new DirectedGraphNode(5);
        
        a.addChild(b);
        b.addChild(c1);
        b.addChild(c2);
        c1.addChild(d);
        d.addChild(e);
        c2.addChild(e);
        
        BreadthFirstSearch.NodeListener<DirectedGraphNode> listener1 =
        new BreadthFirstSearch.NodeListener<DirectedGraphNode>() {
            
            @Override
            public void onBeginSearch(DirectedGraphNode sourceNode) {
                
            }

            @Override
            public void onReach(DirectedGraphNode node) {
                System.out.println("1.onReach: " + node);
            }

            @Override
            public void onExpand(DirectedGraphNode node) {
                System.out.println("1.onExpand: " + node);
            }

            @Override
            public void onEndSearchSuccess(DirectedGraphNode targetNode) {
                System.out.println("1.onEndSearchSuccess: " + targetNode);
            }

            @Override
            public void onEndSearchFailure() {
                
            }
        };
        
        BreadthFirstSearch.NodeListener<DirectedGraphNode> listener2 =
        new BreadthFirstSearch.NodeListener<DirectedGraphNode>() {
            
            @Override
            public void onBeginSearch(DirectedGraphNode sourceNode) {
                System.out.println("2.onBeginSearch: " + sourceNode);
            }

            @Override
            public void onReach(DirectedGraphNode node) {
                
            }

            @Override
            public void onExpand(DirectedGraphNode node) {
                
            }

            @Override
            public void onEndSearchSuccess(DirectedGraphNode targetNode) {
                System.out.println("2.onEndSearchSuccess: " + targetNode);
            }

            @Override
            public void onEndSearchFailure() {
                System.out.println("2.onEndSearchFailure: ");
            }
        };
        
        BreadthFirstSearch.SearchState<DirectedGraphNode> bfs = 
                BreadthFirstSearch.<DirectedGraphNode>findShortestPath()
                                  .from(a)
                                  .to(e)
                                  .addNodeListener(listener1)
                                  .addNodeListener(listener2);
        
        bfs.search();
        
        assertTrue(bfs.isSearchComplete());
        assertEquals(BreadthFirstSearch.SearchState.State.FOUND_PATH,
                     bfs.getState());
        
        List<DirectedGraphNode> path = bfs.getShortestPath();
        assertEquals(4, path.size());
        assertEquals(Arrays.asList(a, b, c2, e), path);
    }
}
