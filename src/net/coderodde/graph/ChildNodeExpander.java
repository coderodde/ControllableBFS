package net.coderodde.graph;

import java.util.Collection;

/**
 * This interface defines the API for graph node types providing a view over its
 * child nodes.
 * 
 * @author Rodion "rodde" Efremov
 * @param <N> the graph node type.
 */
public interface ChildNodeExpander<N> {
    
    /**
     * Returns a view over the child nodes.
     * 
     * @return a collection of child nodes.
     */
    public Collection<N> getChildren();
}
