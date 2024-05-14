package dk.itu.raven.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.Leaf;
import com.github.davidmoten.rtree2.Node;
import com.github.davidmoten.rtree2.NonLeaf;
import com.github.davidmoten.rtree2.RTree;
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
                    if (entry.geometry().intersects(rect)) {
                        return true;
                    }
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

    public static double calculateOverlap(RTree<String, Geometry> tree) {
        Node<String, Geometry> root = tree.root().get();
        Pair<Double, Double> areas = new Pair<>(root.geometry().mbr().area(), 0.0d);
        for (Node<String, Geometry> child : ((NonLeaf<String, Geometry>) root).children()) {
            var child_areas = computeAreas(child);
            areas.first += child_areas.first;
            areas.second += child_areas.second;
        }
        return areas.second / areas.first;
    }

    private static Pair<Double, Double> computeAreas(Node<String, Geometry> node) {
        if (isLeaf(node)) {
            double area = 0.0d;
            for (Entry<String, Geometry> entry : ((Leaf<String, Geometry>) node).entries()) {
                area += entry.geometry().mbr().area();
            }
            return new Pair<>(0.0d, area);
        } else {
            Pair<Double, Double> areas = new Pair<>(node.geometry().mbr().area(), node.geometry().mbr().area());
            for (Node<String, Geometry> child : ((NonLeaf<String, Geometry>) node).children()) {
                var child_areas = computeAreas(child);
                areas.first += child_areas.first;
                areas.second += child_areas.second;

            }
            return areas;
        }
    }

    public static void forEachNode(Node<String, Geometry> node, Consumer<Node<String, Geometry>> consumer) {
        consumer.accept(node);
        if (!isLeaf(node)) {
            for (var child : getChildren(node)) {
                forEachNode(child, consumer);
            }
        }
    }

    public static Pair<Integer, Integer> getAlpha(RTree<String, Geometry> tree, Rectangle rect) {
        RTreeSearch.SearchIterator<String, Geometry> iter = (RTreeSearch.SearchIterator<String, Geometry>) RTreeSearch.search(tree.root().get(), (geom) -> geom.intersects(rect)).iterator();

        while (iter.hasNext()) {
            iter.next();
        }

        return new Pair<>(iter.visited_count, iter.true_count);
    }
}
