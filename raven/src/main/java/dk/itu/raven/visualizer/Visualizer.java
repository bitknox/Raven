package dk.itu.raven.visualizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import com.github.davidmoten.rtree2.Node;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;

import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.io.ShapefileReader.ShapeFileBounds;
import dk.itu.raven.join.AbstractJoinResult;
import dk.itu.raven.join.Square;
import dk.itu.raven.ksquared.K2Raster;
import dk.itu.raven.util.Pair;
import dk.itu.raven.util.TreeExtensions;

/**
 * Class reponsible for making visualizations for shapefile data, join results,
 * and data structures
 */
public class Visualizer {
	int width, height;
	Random r = new Random();

	public Visualizer(int width, int height) {
		this.width = width;
		this.height = height;
		this.r = new Random();
	}

	/**
	 * Draws a join result with with geometries laid on top of the raster result.
	 * 
	 * @param results  join result
	 * @param features vector data features
	 * @param options  visualizer options
	 * @return The imagebuffer
	 */
	public BufferedImage drawResult(AbstractJoinResult results,
			ShapefileReader shapeFileReader, VisualizerOptions options) throws IOException {
		Pair<Iterable<Polygon>, ShapeFileBounds> geometries = shapeFileReader.readShapefile();
		double offsetX = geometries.second.minX > 0 ? -geometries.second.minX : 0;
		double offsetY = geometries.second.minY > 0 ? -geometries.second.minY : 0;
		for (Polygon geom : geometries.first) {
			geom.offset(offsetX, offsetY);
		}
		BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_BYTE_INDEXED);
		Graphics2D rasterGraphics = image.createGraphics();
		rasterGraphics.setColor(Color.white);
		rasterGraphics.fillRect(0, 0, this.width, this.height); // give the whole image a white background
		results.forEach(item -> {
			if (options.useRandomColor) {
				rasterGraphics.setColor(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
			} else {
				rasterGraphics.setColor(options.color);
			}
			for (PixelRange range : item.pixelRanges) {
				rasterGraphics.drawLine(range.x1, range.row, range.x2, range.row);
			}
		});
		rasterGraphics.setColor(Color.RED);
		for (Polygon poly : geometries.first) {
			Point old = poly.getFirst();
			for (Point next : poly) {
				rasterGraphics.drawLine((int) old.x(), (int) old.y(), (int) next.x(), (int) next.y());
				old = next;
			}
		}
		if (options.useOutput) {
			writeImage(image, options.outputPath, options.outputFormat);
		}
		return image;
	}

	/**
	 * Draws the outline of vector data
	 * 
	 * @param features all polygons that should be drawn
	 * @param options  visualizer options
	 * @return A buffered image showing the outlines of all polygons in
	 *         {@code features}
	 */
	public BufferedImage drawShapefile(Iterable<Polygon> features, VisualizerOptions options) throws IOException {
		BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D vectorGraphics = image.createGraphics();
		vectorGraphics.setColor(Color.white);
		vectorGraphics.fillRect(0, 0, this.width, this.height); // give the whole image a white background

		for (Polygon poly : features) {
			if (options.useRandomColor) {
				vectorGraphics.setColor(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
			} else {
				vectorGraphics.setColor(options.color);
			}
			Point old = poly.getFirst();
			for (Point next : poly) {
				vectorGraphics.drawLine((int) old.x(), (int) old.y(), (int) next.x(), (int) next.y());
				old = next;
			}
		}
		if (options.useOutput)

		{
			writeImage(image, options.outputPath, options.outputFormat);
		}
		return image;
	}

	/**
	 * 
	 * @param features The vectorfile features
	 * @return
	 */
	public BufferedImage drawShapefile(Iterable<Polygon> features) throws IOException {
		return drawShapefile(features, new VisualizerOptionsBuilder().build());
	}

	/**
	 * Draws the minimum bounding rectangle for a R*-tree node. (Recursively drawing
	 * children)
	 * 
	 * @param node     The R*-tree node
	 * @param graphics The graphics object we are drawing on top of
	 */
	private void drawMbr(Node<String, Geometry> node, Graphics2D graphics) {
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(new Color(0, 0, 255));
		for (Node<String, Geometry> child : TreeExtensions.getChildren(node)) {
			int width = (int) (child.geometry().mbr().x2() - child.geometry().mbr().x1());
			int height = (int) (child.geometry().mbr().y2() - child.geometry().mbr().y1());
			graphics.drawRect((int) child.geometry().mbr().x1(), (int) child.geometry().mbr().y1(),
					width, height);
			if (!TreeExtensions.isLeaf(child)) {
				drawMbr(child, graphics);
			}
		}
	}

	/**
	 * Draws the k2-raster
	 * 
	 * @param k2Raster       The outer object of a K2 raster tree
	 * @param k2Index        The index of the node in the tree to be drawn
	 * @param rasterBounding The bounding square of the node in the tree
	 * @param level          The number of lower levels yet to be drawn
	 * @param graphics       The graphics object we are drawing on top of
	 */
	private void drawK2Squares(K2Raster k2Raster, int k2Index, Square rasterBounding, int level, Graphics2D graphics) {
		if (level == 0)
			return;
		graphics.setColor(new Color(0, 255, 0));
		int[] children = k2Raster.getChildren(k2Index);
		int childSize = rasterBounding.getSize() / k2Raster.k;
		for (int i = 0; i < children.length; i++) {
			int child = children[i];
			Square childRasterBounding = rasterBounding.getChildSquare(childSize, i, k2Raster.k);
			graphics.drawRect(childRasterBounding.getTopX(), childRasterBounding.getTopY(),
					childRasterBounding.getSize(),
					childRasterBounding.getSize());
			drawK2Squares(k2Raster, child, childRasterBounding, level - 1, graphics);
		}
	}

	/**
	 * 
	 * @param k2Raster The K2-Raster datastructure
	 * @return A bufferd image representing the datastructure
	 */
	public BufferedImage drawK2SquareImage(K2Raster k2Raster, VisualizerOptions options) throws IOException {
		BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, this.width, this.height);
		drawK2Squares(k2Raster, 0, new Square(0, 0, k2Raster.getSize()), 0, graphics);

		writeImage(image, options.outputPath, options.outputFormat);

		return image;
	}

	/**
	 * draws vector shapes, K2Raster tree nodes and R*-tree nodes on top of
	 * eachother
	 * 
	 * @param features the polygons to be drawn
	 * @param tree     the R*-tree, this will be drawn as MBRs for all nodes in the
	 *                 tree
	 * @param k2Raster the K2Raster tree to be drawn
	 * @return A buffered image containing both vector shapes, MBRs from the R*-tree
	 *         and K2 raster nodes drawn on top of eachother
	 */
	public BufferedImage drawVectorRasterOverlap(Iterable<Polygon> features, RTree<String, Geometry> tree,
			K2Raster k2Raster, int k2RecursionDepth, VisualizerOptions options) throws IOException {
		BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();

		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, this.width, this.height); // give the whole image a white background

		drawK2Squares(k2Raster, 0, new Square(0, 0, k2Raster.getSize()), k2RecursionDepth, graphics);

		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(new Color(255, 0, 0));

		for (Polygon poly : features) {
			Point old = poly.getFirst();
			for (Point next : poly) {
				graphics.drawLine((int) old.x(), (int) old.y(), (int) next.x(), (int) next.y());
				old = next;
			}
		}

		drawMbr(tree.root().get(), graphics);
		if (options.useOutput) {
			writeImage(image, options.outputPath, options.outputFormat);
		}

		return image;

	}

	/**
	 * 
	 * @param image        A buffered image to write
	 * @param outputPath   The path where the images is written
	 * @param outputFormat The image format
	 */
	private void writeImage(BufferedImage image, String outputPath, String outputFormat) throws IOException {
		ImageIO.write(image, outputFormat, new File(outputPath));

	}

}
