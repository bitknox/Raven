package dk.itu.raven.join;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.Leaf;
import com.github.davidmoten.rtree2.Node;
import com.github.davidmoten.rtree2.NonLeaf;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.geometry.Size;
import dk.itu.raven.ksquared.AbstractK2Raster;
import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.TreeExtensions;
import dk.itu.raven.util.Tuple4;
import dk.itu.raven.util.Tuple5;

public class RavenJoin extends AbstractRavenJoin {
	private enum QuadOverlapType {
		TotalOverlap,
		PossibleOverlap,
		NoOverlap;
	}

	private enum MBROverlapType {
		TotalOverlap,
		PartialOverlap,
		NoOverlap;
	}

	private AbstractK2Raster k2Raster;
	private RTree<String, Geometry> tree;
	private Offset<Integer> offset;
	private Size imageSize;
	private IRasterFilterFunction function;

	public RavenJoin(AbstractK2Raster k2Raster, RTree<String, Geometry> tree,
			Offset<Integer> offset, Size imageSize) {
		this.k2Raster = k2Raster;
		this.tree = tree;
		this.offset = offset;
		this.imageSize = imageSize;
	}

	public RavenJoin(AbstractK2Raster k2Raster, RTree<String, Geometry> tree, Size imageSize) {
		this(k2Raster, tree, new Offset<>(0, 0), imageSize);
	}

	private void extractRange(List<PixelRange> ranges, int y, int x1, int x2, boolean prob) {
		if (prob) {
			this.k2Raster.searchValuesInWindow(
					y - offset.getY(),
					y - offset.getY(),
					x1 - offset.getX(),
					x2 - offset.getX(),
					function, ranges);
		} else {
			ranges.add(new PixelRange(y, x1, x2));
		}
	}

	/**
	 * 
	 * @param polygon        the vector shape
	 * @param pk             an index of a node in the k2 raster tree
	 * @param rasterBounding teh bounding box of the sub-matrix corresponding to the
	 *                       node with index {@code pk} in the k2 raster tree
	 * @return A collection of pixels that are contained in the vector shape
	 *         described by {@code polygon}
	 */
	protected Collection<PixelRange> extractCellsPolygon(Polygon polygon, int pk, java.awt.Rectangle rasterBounding,
			boolean prob) {
		// 1 on index i * rasterBounding.geetSize() + j if an intersection between a
		// line of the polygon and the line y=j happens at point (i,j)
		// 1 on index i if the left-most pixel of row i intersects the polygon, 0
		// otherwise

		boolean[] inRanges = new boolean[rasterBounding.height + 1]; // we add one to the length to prevent an
																		// out-of-bounds exeption at the end of this
																		// method. This way saves us an if statement.

		Map<Long, Integer> intersections = new TreeMap<>();

		// a line is of the form a*x + b*y = c
		Point old = polygon.getFirst();

		// we run the loop to polygon.size() + 1 because we want to wrap around end at
		// the first point
		for (int i = 1; i < polygon.size() + 1; i++) {
			Point next = polygon.getPoint(i);
			// compute the standard form of the line segment between the points old and next
			double a = (next.y() - old.y());
			double aInv = 1.0 / a;
			double b = (old.x() - next.x());
			double c = a * old.x() + b * old.y();

			int minY = (int) Math.min(rasterBounding.y + rasterBounding.height,
					Math.max(rasterBounding.y, Math.round(Math.min(old.y(), next.y()))));
			int maxY = (int) Math.min(rasterBounding.y + rasterBounding.height,
					Math.max(rasterBounding.y, Math.round(Math.max(old.y(), next.y()))));

			// compute all intersections between the line segment and horizontal pixel lines
			for (int y = minY; y < maxY; y++) {
				double x = (c - b * (y + 0.5)) * aInv;
				int ix = (int) Math.floor(x - rasterBounding.x);
				if (ix <= 0) {
					inRanges[y - rasterBounding.y] = !inRanges[y - rasterBounding.y];
				} else if (ix < rasterBounding.width && ix + rasterBounding.x < imageSize.width) {
					// pack the y and x ordinates together into one long. Sorting a list of these
					// longs will sort the intersections by y, and secondarily by x.
					long yComp = y - rasterBounding.y;
					yComp <<= 32;
					long key = yComp + ix;
					int val = intersections.getOrDefault(key, 0);
					intersections.put(key, val + 1);
				}
			}
			old = next;
		}

		List<PixelRange> ranges = new ArrayList<>();
		int oldY = 0;
		boolean inRange = inRanges[0];
		int start = 0;
		for (var kv : intersections.entrySet()) {
			long k = kv.getKey();
			int v = kv.getValue();
			// reconstruct x and y from packed value
			int x = (int) k;
			int y = (int) (k >>> 32);
			if (y != oldY) { // new pixel-line
				// start by finding out for all pixel-lines between old and new y if it should
				// be joined or not
				for (int j = oldY + 1; j <= y; j++) {
					if (inRange) {
						extractRange(ranges, oldY + rasterBounding.y, start + rasterBounding.x,
								Math.min(rasterBounding.width - 1 + rasterBounding.x,
										imageSize.width - 1),
								prob);
					}
					// start a new pixel-line
					oldY = j;
					inRange = inRanges[j];
					start = 0;
				}
			}
			if ((v % 2) == 0) { // an even number of intersections happen at this point
				if (!inRange) {
					// if a range is ongoing, ignore these intersections, otherwise add this single
					// pixel as a range. If there is an even number of intersections at the edge of
					// the viewport, it should not be added as a single pixel, as that means a
					// vector-shape has both started and ended outside the image.
					extractRange(ranges, y + rasterBounding.y, x + rasterBounding.x,
							x + rasterBounding.x, prob);
				}
			} else {
				if (inRange) {
					inRange = false;
					extractRange(ranges, y + rasterBounding.y, start + rasterBounding.x,
							x + rasterBounding.x - 1, prob);
				} else {
					inRange = true;
					start = x;
				}
			}
		}
		// perform one last check to handle all pixel-lines below the bottom-most
		// intersection
		for (int j = oldY + 1; j <= Math.min(rasterBounding.height, imageSize.height - rasterBounding.y); j++) {
			if (inRange) {
				extractRange(ranges, oldY + rasterBounding.y, start + rasterBounding.x,
						Math.min(rasterBounding.width - 1 + rasterBounding.x,
								imageSize.width - 1),
						prob);
			}
			oldY = j;
			inRange = inRanges[j];
			start = 0;
		}

		return ranges;
	}

	// based loosely on:
	// https://bitbucket.org/bdlabucr/beast/src/master/raptor/src/main/java/edu/ucr/cs/bdlab/raptor/Intersections.java
	private void extractCells(Leaf<String, Geometry> pr, int pk, java.awt.Rectangle rasterBounding,
			JoinResult def, boolean prob) {
		long start = System.nanoTime();
		for (Entry<String, Geometry> entry : ((Leaf<String, Geometry>) pr).entries()) {
			// all geometries we store are polygons
			def.add(new JoinResultItem(entry.geometry(),
					extractCellsPolygon((Polygon) entry.geometry(), pk, rasterBounding, prob)));
		}
		long end = System.nanoTime();
		System.out.println("Extract cells time: " + (end - start) + "ms");
	}

	/**
	 * adds all descendant geometries of a given R*-tree node
	 * 
	 * @param pr             the node of the R*-tree
	 * @param pk             the index of the k2 raster tree node
	 * @param rasterBounding the bounding box of the sum-matrix corresponding to the
	 *                       node with index {@code pk} in the k2 raster tree
	 * @param def            the list all the pixelranges should be added to
	 */
	private void addDescendantsLeaves(NonLeaf<String, Geometry> pr, int pk, java.awt.Rectangle rasterBounding,
			JoinResult def, boolean prob) {
		for (Node<String, Geometry> n : pr.children()) {
			if (TreeExtensions.isLeaf(n)) {
				extractCells((Leaf<String, Geometry>) n, pk, rasterBounding, def, prob);
			} else {
				addDescendantsLeaves((NonLeaf<String, Geometry>) n, pk, rasterBounding, def, prob);
			}
		}
	}

	/**
	 * Finds the smallest node of the k2 raster tree that fully contains the given
	 * bounding box
	 * 
	 * @param k2Index        the index of the starting node in the k2 raster tree.
	 *                       This node should always contain {@code bounding}
	 * @param rasterBounding the bounding box of the sum-matrix corresponding to the
	 *                       node with index {@code k2Index} in the k2 raster tree
	 * @param bounding       the bounding rectangle of some geometry
	 * @param lo             the minimum pixel-value we are looking for
	 * @param hi             the maximum pixel-value we are looking for
	 * @param min            the value of VMin for the node with index
	 *                       {@code k2Index} in the k2 raster tree
	 * @param max            the value of VMax for the node with index
	 *                       {@code k2Index} in the k2 raster tree
	 * @return a 5-tuple (OverlapType, k2Index', rasterBounding', min', max')
	 *         where:
	 *         <ul>
	 *         <li>OverlapType is one of {TotalOverlap, PossibleOverlap,
	 *         NoOverlap}.</li>
	 *         <li>k2Index' is the smallest k2 raster node that fully contains
	 *         {@code bounding}</li>
	 *         <li>rasterBounding' is the bounding box of k2Index'</li>
	 *         <li>min' is the value of VMin for the node with index k2Index' in the
	 *         k2 raster tree</li>
	 *         <li>max' is the value of VMax for the node with index k2Index' in the
	 *         k2 raster tree</li>
	 *         </ul>
	 */
	private Tuple5<QuadOverlapType, Integer, Square, Long, Long> checkQuadrant(int k2Index, Square rasterBounding,
			Rectangle bounding, IRasterFilterFunction function, long min, long max) {
		long vMinMBR = min;
		long vMaxMBR = max;
		int returnedK2Index = k2Index;
		Square returnedrasterBounding = rasterBounding;
		Stack<Tuple4<Integer, Square, Long, Long>> k2Nodes = new Stack<>();
		k2Nodes.push(new Tuple4<>(k2Index, rasterBounding, min, max));
		while (!k2Nodes.empty()) {
			Tuple4<Integer, Square, Long, Long> node = k2Nodes.pop();
			int[] children = k2Raster.getChildren(node.a);
			int childSize = node.b.getSize() / k2Raster.k;
			for (int i = 0; i < children.length; i++) {
				int child = children[i];
				Square childRasterBounding = node.b.getChildSquare(childSize, i, k2Raster.k);
				if (childRasterBounding.contains(bounding)) {
					vMinMBR = k2Raster.computeVMin(node.d, node.c, child);
					vMaxMBR = k2Raster.computeVMax(node.d, child);

					k2Nodes.push(new Tuple4<>(child, childRasterBounding, vMinMBR, vMaxMBR));
					returnedK2Index = child;
					returnedrasterBounding = childRasterBounding;
				}
			}
		}

		if (!function.containsOutside(vMinMBR, vMaxMBR)) {
			Logger.log("total overlap for " + returnedrasterBounding + " with mbr " + bounding, Logger.LogLevel.DEBUG);
			return new Tuple5<>(QuadOverlapType.TotalOverlap, returnedK2Index, returnedrasterBounding, vMinMBR,
					vMaxMBR);
		} else if (!function.containsWithin(vMinMBR, vMaxMBR)) {
			return new Tuple5<>(QuadOverlapType.NoOverlap, returnedK2Index, returnedrasterBounding, vMinMBR, vMaxMBR);
		} else {
			return new Tuple5<>(QuadOverlapType.PossibleOverlap, returnedK2Index, returnedrasterBounding, vMinMBR,
					vMaxMBR);
		}
	}

	/**
	 * More precise than {@code checkQuadrant}, but slower
	 * checks if the MBR of a k2 raster node intersects with a given bounding box
	 * 
	 * @param k2Index        the index of the k2 raster node
	 * @param rasterBounding the bounding box of the sub-matrix corresponding to the
	 *                       node with index {@code k2Index} in the k2 raster tree
	 * @param bounding       the bounding rectangle of the geometry
	 * @param lo             the minimum pixel-value we are looking for
	 * @param hi             the maximum pixel-value we are looking for
	 * @param min            the current minimum pixel-value
	 * @param max            the current maximum pixel-value
	 * @return one of {@code TotalOverlap, PartialOverlap, NoOverlap}
	 */
	private MBROverlapType checkMBR(int k2Index, Square rasterBounding, Rectangle bounding, long min, long max) {
		long vMinMBR = Long.MAX_VALUE;
		long vMaxMBR = Long.MIN_VALUE;

		Stack<Tuple4<Integer, Square, Long, Long>> k2Nodes = new Stack<>();
		k2Nodes.push(new Tuple4<>(k2Index, rasterBounding, min, max));

		while (!k2Nodes.empty()) {
			Tuple4<Integer, Square, Long, Long> node = k2Nodes.pop();
			int[] children = k2Raster.getChildren(node.a);
			int childSize = node.b.getSize() / k2Raster.k;

			if (children.length == 0 && rasterBounding.intersects(bounding)) {
				vMinMBR = Math.min(k2Raster.computeVMax(node.d, node.a), vMinMBR);
				vMaxMBR = Math.max(k2Raster.computeVMax(node.d, node.a), vMaxMBR);
			}

			for (int i = 0; i < children.length; i++) {
				int child = children[i];
				Square childRasterBounding = node.b.getChildSquare(childSize, i, k2Raster.k);

				if (childRasterBounding.intersects(bounding)) {
					long vminVal = k2Raster.computeVMin(node.d, node.c, child);
					long vmaxVal = k2Raster.computeVMax(node.d, child);
					if (childRasterBounding.isContained(bounding)) {
						vMinMBR = Math.min(vminVal, vMinMBR);
						vMaxMBR = Math.max(vmaxVal, vMaxMBR);
					} else {
						k2Nodes.push(new Tuple4<>(child, childRasterBounding, vminVal, vmaxVal));
					}
				}
			}
		}
		if (vMinMBR == Long.MAX_VALUE || vMaxMBR == Long.MIN_VALUE) {
			throw new RuntimeException("rasterBounding was never contained in bounding");
		}
		if (!function.containsOutside(vMinMBR, vMaxMBR)) {
			return MBROverlapType.TotalOverlap;
		} else if (!function.containsWithin(vMinMBR, vMaxMBR)) {
			return MBROverlapType.NoOverlap;
		} else {
			return MBROverlapType.PartialOverlap;
		}
	}

	private java.awt.Rectangle getRectangle(Square rasterBounding) {
		return new java.awt.Rectangle(rasterBounding.getTopX(), rasterBounding.getTopY(),
				Math.min(rasterBounding.getSize(), Math.max(0, imageSize.width - rasterBounding.getTopX())),
				Math.min(rasterBounding.getSize(), Math.max(0, imageSize.height - rasterBounding.getTopY())));
	}

	private boolean intersects(java.awt.Rectangle movedRasterWindow, Rectangle mbr) {
		return !(movedRasterWindow.x >= mbr.x2()
				|| movedRasterWindow.y >= mbr.y2()
				|| movedRasterWindow.x + movedRasterWindow.width <= mbr.x1()
				|| movedRasterWindow.y + movedRasterWindow.height <= mbr.y1());
	}

	// based on:
	// https://journals.plos.org/plosone/article/file?id=10.1371/journal.pone.0226943&type=printable
	/**
	 * joins while filtering based on a given function
	 * 
	 * @param function a function defining
	 * @return a list of Geometries paired with a collection of the pixelranges,
	 *         whose values fall within the given range, that it contains
	 */
	@Override
	protected JoinResult joinImplementation(IRasterFilterFunction function) {
		this.function = function;
		JoinResult def = new JoinResult();
		Stack<Tuple5<Node<String, Geometry>, Integer, Square, Long, Long>> S = new Stack<>();

		Pair<Long, Long> minMax = k2Raster.getValueRange();

		for (Node<String, Geometry> node : TreeExtensions.getChildren(tree.root().get())) {
			S.push(new Tuple5<>(node, 0,
					new Square(offset.getX(), offset.getY(), k2Raster.getSize()), minMax.first,
					minMax.second));
		}

		// Used for early termination. If the vector data does not overlap with BOTH the
		// image and the square k2Raster there will never be an intersection.
		java.awt.Rectangle movedRasterWindow = new java.awt.Rectangle(offset.getX(), offset.getY(),
				Math.min(imageSize.width, k2Raster.getSize()), Math.min(imageSize.height, k2Raster.getSize()));

		while (!S.empty()) {
			Tuple5<Node<String, Geometry>, Integer, Square, Long, Long> p = S.pop();
			if (!intersects(movedRasterWindow, p.a.geometry().mbr()))
				continue;
			long start = System.nanoTime();
			Tuple5<QuadOverlapType, Integer, Square, Long, Long> checked = checkQuadrant(p.b, p.c, p.a.geometry().mbr(),
					function, p.d,
					p.e);
			long end = System.nanoTime();
			System.out.println("Quadrant checking time: " + (end - start) + "ms");
			java.awt.Rectangle rect = getRectangle(checked.c);
			switch (checked.a) {
				case TotalOverlap:
					if (TreeExtensions.isLeaf(p.a)) {

						extractCells((Leaf<String, Geometry>) p.a, checked.b, rect, def, false);
					} else {
						addDescendantsLeaves((NonLeaf<String, Geometry>) p.a, checked.b, rect, def, false);
					}
					break;
				case PossibleOverlap:
					if (!TreeExtensions.isLeaf(p.a)) {
						for (Node<String, Geometry> c : ((NonLeaf<String, Geometry>) p.a).children()) {
							S.push(new Tuple5<Node<String, Geometry>, Integer, Square, Long, Long>(c, checked.b,
									checked.c,
									checked.d, checked.e));
						}
					} else {
						long start2 = System.nanoTime();
						MBROverlapType overlap = checkMBR(checked.b, checked.c, p.a.geometry().mbr(),
								checked.d, checked.e);
						long end2 = System.nanoTime();
						System.out.println("MBR checking time: " + (end2 - start2) + "ms");
						switch (overlap) {
							case TotalOverlap:
								extractCells((Leaf<String, Geometry>) p.a, checked.b, rect, def, false);
								break;
							case PartialOverlap:
								extractCells((Leaf<String, Geometry>) p.a, checked.b, rect, def, true);
								Logger.log(p.a.geometry().mbr(), Logger.LogLevel.DEBUG);
								break;
							case NoOverlap:
								// ignored
								break;
						}
					}
					break;
				case NoOverlap:
					// ignored
					break;
			}
		}

		return def;
	}
}
