package dk.itu.raven.visualizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.github.davidmoten.rtree2.Node;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;

import dk.itu.raven.geometry.GeometryUtil;
import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.geometry.Size;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.io.ShapefileReader.ShapeFileBounds;
import dk.itu.raven.join.IJoinResult;
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

	/**
	 * 
	 * @param width  the ORIGINAL width of the image (that is, the width before it
	 *               was cropped to fit the vector data)
	 * @param height the ORIGINAL height of the image (that is, the height before it
	 *               was cropped to fit the vector data)
	 */
	public Visualizer(int width, int height) {
		this.width = width;
		this.height = height;
		this.r = new Random();
	}

	private Pair<List<Polygon>, ShapeFileBounds> getFeatures(ShapefileReader shapeFileReader)
			throws IOException {
		return shapeFileReader.readShapefile();

	}

	/**
	 * Draws a join result with with geometries laid on top of the raster result.
	 * 
	 * Uses the primary color for the pixels in the join result and the secondary
	 * color for the features
	 * 
	 * @param results  join result
	 * @param features vector data features
	 * @param options  visualizer options
	 * @return The imagebuffer
	 */
	public BufferedImage drawResult(IJoinResult results, ShapefileReader shapeFileReader,
			VisualizerOptions options) throws IOException {
		var pair = getFeatures(shapeFileReader);
		List<Polygon> features = pair.first;
		ShapeFileBounds bounds = pair.second;

		int width = this.width;
		int height = this.height;
		if (options.cropToVector) {
			java.awt.Rectangle rect = GeometryUtil.getWindowRectangle(new Size(width, height), bounds);
			width = rect.width;
			height = rect.height;
		}
		int format;
		if (options.primaryColor.getAlpha() == 255 && options.background.getAlpha() == 255) {
			format = BufferedImage.TYPE_BYTE_INDEXED;
		} else {
			format = BufferedImage.TYPE_INT_ARGB;
		}
		BufferedImage image = new BufferedImage(width, height, format);
		Graphics2D rasterGraphics = image.createGraphics();
		setColor(rasterGraphics, options.background);
		rasterGraphics.fillRect(0, 0, this.width, this.height); // give the whole image a white background

		for (var item : results) {
			for (PixelRange range : item.pixelRanges) {
				setColor(rasterGraphics, options.primaryColor);
				rasterGraphics.drawLine(range.x1, range.row,
						range.x2,
						range.row);
			}
		}

		if (options.drawFeatures) {
			drawFeatures(rasterGraphics, features, options.secondaryColor);
		}
		if (options.useOutput) {
			writeImage(image, options.outputPath, options.outputFormat);
		}

		return image;
	}

	private void setColor(Graphics2D graphics, Color color) {
		// when using a random colour, it will not generate a new one if we just call
		graphics.setColor(new Color(color.getRGB()));
	}

	private void drawFeatures(Graphics2D graphics, Iterable<Polygon> features, Color color) {

		for (Polygon poly : features) {
			setColor(graphics, color);
			Point old = poly.getFirst();
			for (Point next : poly) {
				graphics.drawLine((int) old.x(), (int) old.y(), (int) next.x(), (int) next.y());
				old = next;
			}
		}
	}

	/**
	 * Draws the outline of vector data
	 * 
	 * @param features all polygons that should be drawn
	 * @param options  visualizer options
	 * @return A buffered image showing the outlines of all polygons in
	 *         {@code features}
	 */
	public BufferedImage drawShapefile(ShapefileReader shapeFileReader, VisualizerOptions options) throws IOException {
		List<Polygon> features = getFeatures(shapeFileReader).first;
		BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D vectorGraphics = image.createGraphics();

		setColor(vectorGraphics, options.background);
		vectorGraphics.fillRect(0, 0, this.width, this.height);

		drawFeatures(vectorGraphics, features, options.primaryColor);

		if (options.useOutput) {
			writeImage(image, options.outputPath, options.outputFormat);
		}
		return image;
	}

	/**
	 * 
	 * @param features The vectorfile features
	 * @return
	 */
	public BufferedImage drawShapefile(ShapefileReader shapeFileReader) throws IOException {
		return drawShapefile(shapeFileReader, new VisualizerOptionsBuilder().build());
	}

	/**
	 * Draws the minimum bounding rectangle for a R*-tree node. (Recursively drawing
	 * children)
	 * 
	 * @param node     The R*-tree node
	 * @param graphics The graphics object we are drawing on top of
	 */
	private void drawMbr(Node<String, Geometry> node, Graphics2D graphics, Color color) {
		graphics.setStroke(new BasicStroke(1));
		for (Node<String, Geometry> child : TreeExtensions.getChildren(node)) {
			setColor(graphics, color);
			int width = (int) (child.geometry().mbr().x2() - child.geometry().mbr().x1());
			int height = (int) (child.geometry().mbr().y2() - child.geometry().mbr().y1());
			graphics.drawRect((int) child.geometry().mbr().x1(), (int) child.geometry().mbr().y1(),
					width, height);
			if (!TreeExtensions.isLeaf(child)) {
				drawMbr(child, graphics, color);
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
	private void drawK2Squares(K2Raster k2Raster, int k2Index, Square rasterBounding, int level, Graphics2D graphics,
			Color color) {
		if (level == 0)
			return;
		setColor(graphics, color);
		int[] children = k2Raster.getChildren(k2Index);
		int childSize = rasterBounding.getSize() / k2Raster.k;
		for (int i = 0; i < children.length; i++) {
			int child = children[i];
			Square childRasterBounding = rasterBounding.getChildSquare(childSize, i, k2Raster.k);
			graphics.drawRect(childRasterBounding.getTopX(), childRasterBounding.getTopY(),
					childRasterBounding.getSize(),
					childRasterBounding.getSize());
			drawK2Squares(k2Raster, child, childRasterBounding, level - 1, graphics, color);
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
		setColor(graphics, Color.white);
		graphics.fillRect(0, 0, this.width, this.height);
		drawK2Squares(k2Raster, 0, new Square(0, 0, k2Raster.getSize()), 0, graphics, options.primaryColor);

		writeImage(image, options.outputPath, options.outputFormat);

		return image;
	}

	/**
	 * draws vector shapes, K2Raster tree nodes and R*-tree nodes on top of
	 * eachother
	 * 
	 * Uses the primary color for the K2Raster nodes, the secondary color for the
	 * R*-tree nodes, and the trinary color for the features
	 * 
	 * @param features the polygons to be drawn
	 * @param tree     the R*-tree, this will be drawn as MBRs for all nodes in the
	 *                 tree
	 * @param k2Raster the K2Raster tree to be drawn
	 * @return A buffered image containing both vector shapes, MBRs from the R*-tree
	 *         and K2 raster nodes drawn on top of eachother
	 */
	public BufferedImage drawVectorRasterOverlap(ShapefileReader shapeFileReader, RTree<String, Geometry> tree,
			K2Raster k2Raster, int k2RecursionDepth, VisualizerOptions options) throws IOException {
		Iterable<Polygon> features = getFeatures(shapeFileReader).first;

		BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();

		setColor(graphics, options.background);
		graphics.fillRect(0, 0, this.width, this.height); // give the whole image a white background

		drawK2Squares(k2Raster, 0, new Square(0, 0, k2Raster.getSize()), k2RecursionDepth, graphics,
				options.primaryColor);

		graphics.setStroke(new BasicStroke(1));
		if (options.drawFeatures) {
			drawFeatures(graphics, features, options.trinaryColor);
		}

		drawMbr(tree.root().get(), graphics, options.secondaryColor);

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
		ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
		ImageWriteParam param = writer.getDefaultWriteParam();
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(0.5f); // Adjust the quality parameter as needed

		File fOutputFile = new File(outputPath);
		ImageOutputStream ios = ImageIO.createImageOutputStream(fOutputFile);
		writer.setOutput(ios);
		writer.write(null, new IIOImage(image, null, null), param);
		writer.dispose();
		ios.close();
	}

}
