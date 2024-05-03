package dk.itu.raven.io;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.FileImageInputStream;

import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Logger.LogLevel;
import dk.itu.raven.util.matrix.AwtRasterMatrix;
import dk.itu.raven.util.matrix.Matrix;

public class ImageIORasterReader extends FileRasterReader {

    public ImageIORasterReader(File directory) throws IOException {
        super(directory);
    }

    @Override
    public Matrix readRasters(Rectangle rect) throws IOException {
        FileImageInputStream stream = new FileImageInputStream(tiff);
        ImageReader reader = ImageIO.getImageReaders(stream).next();
        reader.setInput(stream);

        ImageReadParam param = reader.getDefaultReadParam();
        int minX = Math.max(rect.x, 0);
        int minY = Math.max(rect.y, 0);
        int maxX = Math.min(rect.x + rect.width, reader.getWidth(0));
        int maxY = Math.min(rect.y + rect.height, reader.getHeight(0));
        param.setSourceRegion(new java.awt.Rectangle(minX, minY, maxX - minX, maxY - minY));
        BufferedImage img = reader.read(0, param);
        reader.dispose();
        stream.close();
        return new AwtRasterMatrix(img.getData());
    }

    @Override
    public ImageMetadata readImageMetadata() throws IOException {
        long start = System.currentTimeMillis();

        FileImageInputStream stream = new FileImageInputStream(tiff);
        ImageReader reader = ImageIO.getImageReaders(stream).next();
        reader.setInput(stream);

        ImageTypeSpecifier imageType = reader.getRawImageType(0);

        int width = reader.getWidth(0);
        int height = reader.getHeight(0);

        int samplesPerPixel = imageType.getNumBands();
        int[] bitsPerSample = new int[samplesPerPixel];

        Logger.log("bits:", LogLevel.DEBUG);
        for (int i = 0; i < samplesPerPixel; i++) {
            bitsPerSample[i] = imageType.getBitsPerBand(i);
            Logger.log("  " + bitsPerSample[i], LogLevel.DEBUG);
        }

        long end = System.currentTimeMillis();
        Logger.log("Read tiff in " + (end - start) + "ms", Logger.LogLevel.INFO);

        reader.dispose();
        stream.close();
        return new ImageMetadata(width, height, samplesPerPixel, bitsPerSample, getDirectoryName());
    }

    public ColorModel getColorModel() throws IOException {
        FileImageInputStream stream = new FileImageInputStream(tiff);
        ImageReader reader = ImageIO.getImageReaders(stream).next();
        reader.setInput(stream);

        ImageReadParam param = reader.getDefaultReadParam();
        int minX = 0;
        int minY = 0;
        int maxX = Math.min(1, reader.getWidth(0));
        int maxY = Math.min(1, reader.getHeight(0));
        param.setSourceRegion(new java.awt.Rectangle(minX, minY, maxX - minX, maxY - minY));
        BufferedImage img = reader.read(0, param);
        return img.getColorModel();
    }
}
