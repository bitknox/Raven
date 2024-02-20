package dk.itu.raven.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;

import org.w3c.dom.Element;

import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.SpatialDataChunk;
import dk.itu.raven.util.Logger;
import dk.itu.raven.util.matrix.AwtRasterMatrix;
import dk.itu.raven.util.matrix.Matrix;

public class ImageIORasterReader extends FileRasterReader {

    public ImageIORasterReader(File directory) throws IOException {
        super(directory);
    }

    @Override
    public Matrix readRasters(Rectangle rect) throws IOException {
        ImageReader reader = ImageIO.getImageReaders(new FileImageInputStream(tiff)).next();
        reader.setInput(new FileImageInputStream(tiff));
        ImageReadParam param = reader.getDefaultReadParam();
        int minX = (int) Math.max(rect.x1(), 0.0);
        int minY = (int) Math.max(rect.y1(), 0.0);
        int maxX = (int) Math.ceil(Math.min(rect.x2(), reader.getWidth(0)));
        int maxY = (int) Math.ceil(Math.min(rect.y2(), reader.getHeight(0)));
        param.setSourceRegion(new java.awt.Rectangle(minX, minY, maxX - minX, maxY - minY));
        BufferedImage img = reader.read(0, param);
        return new AwtRasterMatrix(img.getData());
    }

    @Override
    public ImageMetadata readImageMetadata() throws IOException {
        long start = System.currentTimeMillis();

        ImageReader reader = ImageIO.getImageReaders(new FileImageInputStream(tiff)).next();
        reader.setInput(new FileImageInputStream(tiff));

        IIOMetadata metadata = reader.getImageMetadata(0);
        Element metadataNode = (Element) metadata.getAsTree("javax_imageio_1.0");

        int width = reader.getWidth(0);
        int height = reader.getHeight(0);
        int samplesPerPixel = Integer.parseInt(metadataNode.getElementsByTagName("NumChannels").item(0).getAttributes()
                .getNamedItem("value").getNodeValue());
        int[] bitsPerSample = new int[samplesPerPixel];

        String[] bitsPerSampleString = metadataNode.getElementsByTagName("BitsPerSample").item(0).getAttributes()
                .getNamedItem("value").getNodeValue().split(" ");

        for (int i = 0; i < samplesPerPixel; i++) {
            bitsPerSample[i] = Integer.parseInt(bitsPerSampleString[i]);
        }

        long end = System.currentTimeMillis();
        Logger.log("Read tiff in " + (end - start) + "ms", Logger.LogLevel.INFO);

        return new ImageMetadata(width, height, samplesPerPixel, bitsPerSample);
    }
}
