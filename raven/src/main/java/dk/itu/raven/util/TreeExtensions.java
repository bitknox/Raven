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

    public static <T, S extends Geometry> Iterable<Node<T, S>> getChildren(Node<T, S> node) {
        if (node instanceof NonLeaf) {
            return ((NonLeaf<T, S>) node).children();
        } else {
            List<Node<T, S>> res = new ArrayList<>();
            res.add(node);
            return res;
        }
    }

    public static <T, S extends Geometry> boolean isLeaf(Node<T, S> node) {
        return node instanceof Leaf;
    }

    public static <T, S extends Geometry> boolean isNonLeaf(Node<T, S> node) {
        return node instanceof NonLeaf;
    }

    public static <T, S extends Geometry> boolean intersectsOne(Node<T, S> node, Rectangle rect) {
        if (node.geometry().intersects(rect)) {
            if (isLeaf(node)) {
                for (Entry<T, S> entry : ((Leaf<T, S>) node).entries()) {
                    if (entry.geometry().intersects(rect)) {
                        return true;
                    }
                }
                return false;
            } else {
                for (Node<T, S> child : ((NonLeaf<T, S>) node).children()) {
                    if (intersectsOne(child, rect)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
