package dk.itu.raven.util;

import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.Leaf;
import com.github.davidmoten.rtree2.Node;
import com.github.davidmoten.rtree2.NonLeaf;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Rectangle;

/**
 * A class that provides extensions for the RTree library.
 * 
 */
public class TreeExtensions {
    public static Iterable<Node<String, Geometry>> getChildren(Node<String, Geometry> node) {
        if (node instanceof NonLeaf) {
            return ((NonLeaf<String, Geometry>) node).children();
        } else {
            List<Node<String, Geometry>> res = new ArrayList<>();
            res.add(node);
            return res;
        }
    }

    public static boolean isLeaf(Node<String, Geometry> node) {
        return node instanceof Leaf;
    }

    public static boolean isNonLeaf(Node<String, Geometry> node) {
        return node instanceof NonLeaf;
    }

    public static boolean intersectsOne(Node<String, Geometry> node, Rectangle rect) {
        if (node.geometry().intersects(rect)) {
            if (isLeaf(node)) {
                for (Entry<String, Geometry> entry : ((Leaf<String, Geometry>) node).entries()) {
                    if (entry.geometry().intersects(rect))
                        return true;
                }
                return false;
            } else {
                for (Node<String, Geometry> child : ((NonLeaf<String, Geometry>) node).children()) {
                    if (intersectsOne(child, rect)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}