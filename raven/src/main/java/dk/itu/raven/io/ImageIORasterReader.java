package dk.itu.raven.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.FileImageInputStream;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;

import com.github.davidmoten.rtree2.geometry.Rectangle;

import dk.itu.raven.util.Logger;
import dk.itu.raven.util.Logger.LogLevel;
import dk.itu.raven.util.matrix.AwtRasterMatrix;
import dk.itu.raven.util.matrix.Matrix;

import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi;

public class ImageIORasterReader extends FileRasterReader {

    public ImageIORasterReader(File directory) throws IOException {
        super(directory);
    }

    @Override
    public Matrix readRasters(Rectangle rect) throws IOException {
        ImageReader reader = ImageIO.getImageReaders(new FileImageInputStream(tiff)).next();
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

        // ImageReader reader = this.readerSpi.createReaderInstance();
        // this.inStream.mark();
        // reader.setInput(new FileImageInputStream(tiff));
        // IIOMetadata iioMetadata = reader.getImageMetadata(0);
        // ImageReaderSpi spi = TIFFImageReaderSpi;
        ImageReader reader = ImageIO.getImageReaders(new FileImageInputStream(tiff)).next();
        reader.setInput(new FileImageInputStream(tiff));

        IIOMetadata metadata = reader.getImageMetadata(0);
        String formatString = metadata.getNativeMetadataFormatName();
        Element metadataNode = (Element) metadata.getAsTree("javax_imageio_1.0");
        NodeList lst = metadataNode.getElementsByTagName("*");

        for (int i = 0; i < lst.getLength(); i++) {
            if (lst.item(i).getNodeName().equals("PaletteEntry"))
                continue;
            Logger.log(lst.item(i).getNodeName() + ": ", LogLevel.DEBUG);
            for (int j = 0; j < lst.item(i).getAttributes().getLength(); j++) {
                Logger.log("  " + lst.item(i).getAttributes().item(j).getNodeName() + ": "
                        + lst.item(i).getAttributes().item(j).getNodeValue(), LogLevel.DEBUG);
            }
        }

        int width = Integer
                .parseInt(metadataNode.getElementsByTagName("ImageWidth").item(0).getAttributes().getNamedItem("value")
                        .getNodeValue());
        int height = Integer
                .parseInt(metadataNode.getElementsByTagName("ImageLength").item(0).getAttributes().getNamedItem("value")
                        .getNodeValue());
        int samplesPerPixel = Integer
                .parseInt(metadataNode.getElementsByTagName("NumChannels").item(0).getAttributes().getNamedItem("value")
                        .getNodeValue());
        int[] bitsPerSample = new int[samplesPerPixel];

        String[] split = metadataNode.getElementsByTagName("BitsPerSample").item(0).getAttributes()
                .getNamedItem("value").getNodeValue()
                .split(" ");
        for (int i = 0; i < samplesPerPixel; i++) {
            bitsPerSample[i] = Integer.parseInt(split[i]);
        }
        long end = System.currentTimeMillis();

        Logger.log("Read tiff in " + (end - start) + "ms", Logger.LogLevel.INFO);
        return new ImageMetadata(width, height, samplesPerPixel, bitsPerSample);
    }

}
