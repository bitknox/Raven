package dk.itu.raven.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.davidmoten.rtree2.Node;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Rectangle;

public class TreeExtensionsTest {
	private RTree<String, Geometry> rtree;

	@BeforeEach
	public void setUp() {
		// Create an RTree
		rtree = RTree.star().maxChildren(3).create();

		// Add some test data
		rtree = rtree.add("test1", Geometries.rectangle(0, 0, 5, 5));
		rtree = rtree.add("test2", Geometries.rectangle(1, 1, 4, 4));
		rtree = rtree.add("test3", Geometries.rectangle(11, 11, 15, 15));
		rtree = rtree.add("test4", Geometries.rectangle(6, 6, 8, 8));
	}

	@Test
	public void testIntersectsOnePositive() {
		// Test when there is an intersection between the node's geometry and the
		// rectangle
		Rectangle testRect = Geometries.rectangle(0, 0, 5, 5);
		Node<String, Geometry> rootNode = rtree.root().get();

		assertTrue(TreeExtensions.intersectsOne(rootNode, testRect));
	}

	@Test
	public void testIntersectsOneNegative() {
		// Test when there is no intersection between the node's geometry and the
		// rectangle
		Rectangle testRect = Geometries.rectangle(16, 16, 20, 20);
		Node<String, Geometry> rootNode = rtree.root().get();

		assertFalse(TreeExtensions.intersectsOne(rootNode, testRect));
	}

	@Test
	public void testIntersectsOneNestedPositive() {
		// Test when there is a positive intersection in a nested structure
		Rectangle testRect = Geometries.rectangle(2, 2, 3, 3);
		Node<String, Geometry> rootNode = rtree.root().get();

		assertTrue(TreeExtensions.intersectsOne(rootNode, testRect));
	}

	@Test
	public void testIntersectsOneNestedNegative() {
		// Test when there is a positive intersection in a nested structure
		Rectangle testRect = Geometries.rectangle(9, 9, 10, 10);
		Node<String, Geometry> rootNode = rtree.root().get();

		assertFalse(TreeExtensions.intersectsOne(rootNode, testRect));
	}

}