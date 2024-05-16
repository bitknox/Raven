package dk.itu.raven.visualizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.referencing.operation.transform.AffineTransform2D;

import com.github.davidmoten.rtree2.Entries;
import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.Node;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.api.InternalApi;
import dk.itu.raven.geometry.GeometryUtil;
import dk.itu.raven.geometry.Polygon;
import dk.itu.raven.geometry.Size;
import dk.itu.raven.io.ImageIORasterReader;
import dk.itu.raven.io.Reprojector;
import dk.itu.raven.io.ShapefileReader;
import dk.itu.raven.io.ShapefileReader.ShapeFileBounds;
import dk.itu.raven.io.TFWFormat;
import dk.itu.raven.io.VectorData;
import dk.itu.raven.join.Square;
import dk.itu.raven.join.results.IJoinResult;
import dk.itu.raven.join.results.IResult;
import dk.itu.raven.join.results.PixelRangeValue;
import dk.itu.raven.ksquared.AbstractK2Raster;
import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Logger.LogLevel;
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
     * @param width the ORIGINAL width of the image (that is, the width before
     * it was cropped to fit the vector data)
     * @param height the ORIGINAL height of the image (that is, the height
     * before it was cropped to fit the vector data)
     */
    public Visualizer(int width, int height) {
        this.width = width;
        this.height = height;
        this.r = new Random();
    }

    private VectorData getFeatures(ShapefileReader shapeFileReader)
            throws IOException {
        return shapeFileReader.readShapefile();

    }

    /**
     * Draws a join result with with geometries laid on top of the raster
     * result.
     *
     * Uses the primary color for the pixels in the join result and the
     * secondary color for the features
     *
     * @param results join result
     * @param features vector data features
     * @param options visualizer options
     * @return The imagebuffer
     */
    public void drawResult(IJoinResult results, ShapefileReader shapeFileReader,
            VisualizerOptions options) throws IOException {
        var vectorData = getFeatures(shapeFileReader);
        List<Entry<String, Geometry>> features = vectorData.getFeatures();
        ShapeFileBounds bounds = vectorData.getBounds();

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

        Set<File> files = new HashSet<>();
        results.asMemoryAllocatedResult().forAll(item -> {
            if (item.file.isPresent() && !files.contains(item.file.get())) {
                files.add(item.file.get());
            }
        });
        for (File file : files) {
            BufferedImage image = new BufferedImage(width, height, format);
            Graphics2D rasterGraphics = image.createGraphics();
            setColor(rasterGraphics, options.background);
            rasterGraphics.fillRect(0, 0, this.width, this.height); // give the whole image a white background
            IJoinResult currentFileResults = results.filter(jri -> jri.file.isPresent() && jri.file.get().equals(file));

            Logger.log("drawing " + file.getAbsolutePath(), LogLevel.INFO);

            ImageIORasterReader reader = new ImageIORasterReader(file);
            Optional<IndexColorModel> indexColorModel = Optional.empty();
            if (options.useOriginalColours) {
                ColorModel colorModel = reader.getColorModel();
                if (colorModel instanceof IndexColorModel) {
                    indexColorModel = Optional.of((IndexColorModel) colorModel);
                    image = new BufferedImage(width, height, format, indexColorModel.get());
                    rasterGraphics = image.createGraphics();
                    rasterGraphics.fillRect(0, 0, this.width, this.height); // give the whole image a white background
                } else {
                    Logger.log("No IndexColorModel", LogLevel.WARNING);

                }
            }
            drawResults(currentFileResults, options, rasterGraphics, indexColorModel);

            if (options.drawFeatures) {

                CoordinateReferenceSystem targetCRS = reader.getCRS();
                TFWFormat g2m = reader.getG2M();
                MathTransform transform = null;
                try {
                    transform = Reprojector.calculateFullTransform(vectorData.getCRS(), targetCRS,
                            g2m);
                    MathTransform inverseTransform = transform.inverse();
                    double[] topLeftPixel = new double[]{0, 0};
                    double[] bottomRightPixel = new double[]{reader.getImageMetadata().getWidth(),
                        reader.getImageMetadata().getHeight()};
                    double[] topLeftLatLong = new double[2];
                    double[] bottomRightLatLong = new double[2];

                    inverseTransform.transform(topLeftPixel, 0, topLeftLatLong, 0, 1);
                    inverseTransform.transform(bottomRightPixel, 0, bottomRightLatLong, 0, 1);

                    Rectangle rasterBounds = Geometries.rectangle(topLeftLatLong[0], bottomRightLatLong[1],
                            bottomRightLatLong[0], topLeftLatLong[1]);
                    List<Polygon> overlapping = new ArrayList<>();
                    for (Entry<String, Geometry> poly : features) {
                        if (rasterBounds.intersects(poly.geometry().mbr())) {
                            overlapping.add(((Polygon) poly.geometry()).transform(transform));
                        }
                    }
                    drawFeatures(rasterGraphics, overlapping, options.secondaryColor);
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.log("Cannot draw vector data", LogLevel.ERROR);
                }

            }
            if (options.useOutput) {
                writeImage(image, options,
                        file.getName());
            }
        }
    }

    private void setColor(Graphics2D graphics, Color color, int size) {
        graphics.setColor(new Color(((LengthColor) color).getRGB(size)));
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

    private void scaleEntries(List<Entry<String, Geometry>> features) {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (Entry<String, Geometry> poly : features) {
            Rectangle mbr = poly.geometry().mbr();
            minX = Math.min(minX, mbr.x1());
            maxX = Math.max(maxX, mbr.x2());
            minY = Math.min(minY, mbr.y1());
            maxY = Math.max(maxY, mbr.y2());
        }

        double scaleX = this.width / (maxX - minX);
        double scaleY = this.height / (maxY - minY);
        double scale = Math.min(scaleX, scaleY);
        MathTransform transform = new AffineTransform2D(scale, 0, 0, -scale, (this.width - scale * (minX + maxX)) / 2,
                (this.height + scale * (minY + maxY)) / 2);
        try {
            for (int i = 0; i < features.size(); i++) {
                Polygon poly = ((Polygon) features.get(i).geometry()).transform(transform);
                features.set(i, Entries.entry(null, poly));
            }
        } catch (Exception e) {
            Logger.log(e, LogLevel.ERROR);
            Logger.log("Unable to scale vector data to fit image size", LogLevel.ERROR);
        }
    }

    private void scale(List<Polygon> features) {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (Polygon poly : features) {
            minX = Math.min(minX, poly.mbr().x1());
            maxX = Math.max(maxX, poly.mbr().x2());
            minY = Math.min(minY, poly.mbr().y1());
            maxY = Math.max(maxY, poly.mbr().y2());
        }

        double scaleX = this.width / (maxX - minX);
        double scaleY = this.height / (maxY - minY);
        double scale = Math.min(scaleX, scaleY);
        MathTransform transform = new AffineTransform2D(scale, 0, 0, -scale, (this.width - scale * (minX + maxX)) / 2,
                (this.height + scale * (minY + maxY)) / 2);
        try {
            for (int i = 0; i < features.size(); i++) {
                features.set(i, features.get(i).transform(transform));
            }
        } catch (Exception e) {
            Logger.log(e, LogLevel.ERROR);
            Logger.log("Unable to scale vector data to fit image size", LogLevel.ERROR);
        }
    }

    private List<Polygon> getPolygons(List<Entry<String, Geometry>> entries) {
        List<Polygon> polygons = new ArrayList<>();
        for (var entry : entries) {
            polygons.add((Polygon) entry.geometry());
        }
        return polygons;
    }

    /**
     * Draws the outline of vector data
     *
     * @param features a reader returning all polygons that should be drawn
     * @param options visualizer options
     * @return A buffered image showing the outlines of all polygons in
     * {@code features}
     */
    public BufferedImage drawShapefile(ShapefileReader shapeFileReader, VisualizerOptions options) throws IOException {
        List<Polygon> polygons = getPolygons(getFeatures(shapeFileReader).getFeatures());
        scale(polygons);

        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D vectorGraphics = image.createGraphics();

        setColor(vectorGraphics, options.background);
        vectorGraphics.fillRect(0, 0, this.width, this.height);

        drawFeatures(vectorGraphics, polygons, options.primaryColor);

        if (options.useOutput) {
            writeImage(image, options, "shapefile");
        }
        return image;
    }

    public BufferedImage drawRtree(ShapefileReader reader, int minChildren, int maxChildren, VisualizerOptions options)
            throws IOException {
        var entries = reader.readShapefile().getFeatures();
        scaleEntries(entries);
        List<Polygon> features = getPolygons(entries);
        return drawRtree(InternalApi.generateRTree(entries, minChildren, maxChildren), features, options);
    }

    public BufferedImage drawRtree(RTree<String, Geometry> rtree, List<Polygon> features, VisualizerOptions options)
            throws IOException {
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D vectorGraphics = image.createGraphics();

        setColor(vectorGraphics, options.background);
        vectorGraphics.fillRect(0, 0, this.width, this.height);

        if (options.drawFeatures) {
            drawFeatures(vectorGraphics, features, options.secondaryColor);
        }

        int lineWidth = rtree.calculateDepth();
        drawMbr(rtree.root().get(), vectorGraphics, options.primaryColor, lineWidth);

        if (options.useOutput) {
            writeImage(image, options, "rtree");
        }
        return image;
    }

    /**
     * Draws the minimum bounding rectangle for a R*-tree node. (Recursively
     * drawing children)
     *
     * @param node The R*-tree node
     * @param graphics The graphics object we are drawing on top of
     */
    private void drawMbr(Node<String, Geometry> node, Graphics2D graphics, Color color, int lineWidth) {
        graphics.setStroke(new BasicStroke(lineWidth));
        for (Node<String, Geometry> child : TreeExtensions.getChildren(node)) {
            setColor(graphics, color);
            int width = (int) (child.geometry().mbr().x2() - child.geometry().mbr().x1());
            int height = (int) (child.geometry().mbr().y2() - child.geometry().mbr().y1());
            graphics.drawRect((int) child.geometry().mbr().x1(), (int) child.geometry().mbr().y1(),
                    width, height);
            if (!TreeExtensions.isLeaf(child)) {
                drawMbr(child, graphics, color, Math.max(1, lineWidth - 1));
            }
        }
        graphics.setStroke(new BasicStroke(1));
    }

    /**
     * Draws the k2-raster
     *
     * @param k2Raster The outer object of a K2 raster tree
     * @param k2Index The index of the node in the tree to be drawn
     * @param rasterBounding The bounding square of the node in the tree
     * @param level The number of lower levels yet to be drawn
     * @param graphics The graphics object we are drawing on top of
     */
    private void drawK2Squares(AbstractK2Raster k2Raster, int k2Index, Square rasterBounding, int level,
            Graphics2D graphics, Color color) {
        setColor(graphics, color);
        int[] children = k2Raster.getChildren(k2Index);
        int childSize = rasterBounding.getSize() / k2Raster.k;
        for (int i = 0; i < children.length; i++) {
            int child = children[i];
            Square childRasterBounding = rasterBounding.getChildSquare(childSize, i, k2Raster.k);
            graphics.drawRect(childRasterBounding.getTopX(), childRasterBounding.getTopY(),
                    childRasterBounding.getSize(),
                    childRasterBounding.getSize());
            drawK2Squares(k2Raster, child, childRasterBounding, level + 1, graphics, color);
        }
    }

    /**
     *
     * @param k2Raster The K2-Raster datastructure
     * @return A bufferd image representing the datastructure
     */
    public BufferedImage drawK2SquareImage(AbstractK2Raster k2Raster, VisualizerOptions options) throws IOException {
        BufferedImage image = new BufferedImage(k2Raster.getSize(), k2Raster.getSize(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        setColor(graphics, Color.white);
        graphics.fillRect(0, 0, k2Raster.getSize(), k2Raster.getSize());
        drawK2Squares(k2Raster, 0, new Square(0, 0, k2Raster.getSize()), 0, graphics, options.primaryColor);

        writeImage(image, options, "K2");

        return image;
    }

    private void drawResults(IJoinResult results, VisualizerOptions options, Graphics2D graphics,
            Optional<IndexColorModel> indexColourModel) {
        Set<Long> colours = new HashSet<>();
        for (var item : results) {
            for (IResult value : item.pixelRanges) {
                if (indexColourModel.isPresent() && value.getValue().isPresent()
                        && indexColourModel.get().getPixelSize() == 8) {
                    colours.add(value.getValue().get());
                    setColor(graphics,
                            new Color(indexColourModel.get().getRGB(value.getValue().get().intValue())));
                } else {
                    setColor(graphics, options.primaryColor, ((PixelRangeValue) value).x2 - ((PixelRangeValue) value).x1);
                }
                value.draw(graphics);
            }
        }

        Logger.log(colours.size(), LogLevel.DEBUG);
        for (Long colour : colours) {
            Logger.log(colour, LogLevel.DEBUG);
        }
    }

    /**
     *
     * @param image A buffered image to write
     * @param outputPath The path where the images is written
     * @param outputFormat The image format
     */
    private void writeImage(BufferedImage image, VisualizerOptions options, String name) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName(options.outputFormat).next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.5f); // Adjust the quality parameter as needed

        new File(options.outputPath).mkdir();

        File fOutputFile = new File(options.outputPath + "/" + name + "." + options.outputFormat);
        ImageOutputStream ios = ImageIO.createImageOutputStream(fOutputFile);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(image, null, null), param);
        writer.dispose();
        ios.close();
    }

}
