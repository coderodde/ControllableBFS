package net.coderodde.graph;

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BreadthFirstSearchTest {
    
    @Test
    public void test() {
        DirectedGraphNode a  = new DirectedGraphNode();
        DirectedGraphNode b  = new DirectedGraphNode();
        DirectedGraphNode c1 = new DirectedGraphNode();
        DirectedGraphNode c2 = new DirectedGraphNode();
        DirectedGraphNode d  = new DirectedGraphNode();
        DirectedGraphNode e  = new DirectedGraphNode();
        
        a.addChild(b);
        b.addChild(c1);
        b.addChild(c2);
        c1.addChild(d);
        d.addChild(e);
        c2.addChild(e);
        
        BreadthFirstSearch.SearchState<DirectedGraphNode> bfs = 
                BreadthFirstSearch.<DirectedGraphNode>findShortestPath()
                                  .from(a)
                                  .to(e);
        
        bfs.search();
        
        assertTrue(bfs.isSearchComplete());
        assertEquals(BreadthFirstSearch.SearchState.State.FOUND_PATH,
                     bfs.getState());
        
        List<DirectedGraphNode> path = bfs.getShortestPath();
        assertEquals(4, path.size());
        assertEquals(Arrays.asList(a, b, c2, e), path);
    }
}
