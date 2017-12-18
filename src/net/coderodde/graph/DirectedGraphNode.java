package net.coderodde.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This class implements a directed graph node type using the adjacency lists.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Dec 18, 2017)
 */
public final class DirectedGraphNode 
        implements ChildNodeExpander<DirectedGraphNode> {
    
    private final int id;
    
    public DirectedGraphNode(int id) {
        this.id = id;
    }
    
    private final List<DirectedGraphNode> children = new ArrayList<>();
    private final List<DirectedGraphNode> immutableChildrenView = 
            Collections.<DirectedGraphNode>unmodifiableList(children);
    
    public void addChild(DirectedGraphNode child) {
        children.add(Objects.requireNonNull(child));
    }
    
    @Override
    public List<DirectedGraphNode> getChildren() {
        return immutableChildrenView;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } 
        
        if (o == null) {
            return false;
        } 
        
        if (!getClass().equals(o.getClass())) {
            return false;
        }
        
        DirectedGraphNode other = (DirectedGraphNode) o;
        return id == other.id;
    }
    
    @Override
    public int hashCode() {
        return id;
    }
    
    @Override
    public String toString() {
        return Integer.toString(id);
    }
}
