package dk.itu.raven.util;

import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.Leaf;
import com.github.davidmoten.rtree2.Node;
import com.github.davidmoten.rtree2.NonLeaf;
import com.github.davidmoten.rtree2.geometry.Geometry;

final class RTreeSearch {

    //Mutable, not thread-safe
    static final class NodePosition<T, S extends Geometry> {

        private final Node<T, S> node;
        private int position;

        NodePosition(Node<T, S> node, int position) {
            Preconditions.checkNotNull(node);
            this.node = node;
            this.position = position;
        }

        Node<T, S> node() {
            return node;
        }

        int position() {
            return position;
        }

        boolean hasRemaining() {
            return position != node.count();
        }

        void setPosition(int position) {
            this.position = position;
        }

        @Override
        public String toString() {
            String builder = "NodePosition [node="
                    + node
                    + ", position="
                    + position
                    + "]";
            return builder;
        }

    }

    private RTreeSearch() {
        // prevent instantiation
    }

    static <T, S extends Geometry> Iterable<Entry<T, S>> search(Node<T, S> node,
            Predicate<? super Geometry> condition) {
        return new SearchIterable<>(node, condition);
    }

    static final class SearchIterable<T, S extends Geometry> implements Iterable<Entry<T, S>> {

        private final Node<T, S> node;
        private final Predicate<? super Geometry> condition;

        SearchIterable(Node<T, S> node, Predicate<? super Geometry> condition) {
            this.node = node;
            this.condition = condition;
        }

        @Override
        public Iterator<Entry<T, S>> iterator() {
            return new SearchIterator<>(node, condition);
        }

    }

    static final class SearchIterator<T, S extends Geometry> implements Iterator<Entry<T, S>> {

        public int visited_count = 0;
        public int true_count = 0;
        private Set<Node<T, S>> seen = new HashSet<>();
        private final Predicate<? super Geometry> condition;
        private final Deque<NodePosition<T, S>> stack;
        private Entry<T, S> next;

        SearchIterator(Node<T, S> node, Predicate<? super Geometry> condition) {
            this.condition = condition;
            this.stack = new LinkedList<>();
            stack.push(new NodePosition<>(node, 0));
        }

        @Override
        public boolean hasNext() {
            load();
            return next != null;
        }

        @Override
        public Entry<T, S> next() {
            load();
            if (next == null) {
                throw new NoSuchElementException();
            } else {
                Entry<T, S> v = next;
                next = null;
                return v;
            }
        }

        private void load() {
            if (next == null) {
                next = search();
            }
        }

        private Entry<T, S> search() {
            while (!stack.isEmpty()) {
                NodePosition<T, S> np = stack.peek();
                if (!np.hasRemaining()) {
                    // handle after last in node
                    searchAfterLastInNode();
                } else if (np.node() instanceof NonLeaf) {
                    // handle non-leaf
                    searchNonLeaf(np);
                } else {
                    // handle leaf
                    Entry<T, S> v = searchLeaf(np);
                    if (v != null) {
                        return v;
                    }
                }
            }
            return null;
        }

        private Entry<T, S> searchLeaf(NodePosition<T, S> np) {
            visited_count++;
            int i = np.position();
            Leaf<T, S> leaf = (Leaf<T, S>) np.node();
            do {
                Entry<T, S> entry = leaf.entry(i);
                if (condition.test(entry.geometry())) {
                    np.setPosition(i + 1);
                    if (!seen.contains(leaf)) {
                        seen.add(leaf);
                        true_count++;
                    }
                    return entry;
                }
                i++;
            } while (i < leaf.count());
            np.setPosition(i);
            return null;
        }

        private void searchNonLeaf(NodePosition<T, S> np) {
            Node<T, S> child = ((NonLeaf<T, S>) np.node()).child(np.position());
            if (condition.test(child.geometry())) {
                stack.push(new NodePosition<>(child, 0));
            } else {
                np.setPosition(np.position() + 1);
            }
        }

        private void searchAfterLastInNode() {
            stack.pop();
            if (!stack.isEmpty()) {
                NodePosition<T, S> previous = stack.peek();
                previous.setPosition(previous.position() + 1);
            }
        }

    }

}
