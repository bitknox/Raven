package dk.itu.raven.io;

import java.util.Optional;

/**
 * ImageMetadata
 */
public class ImageMetadata {

    private int width;
    private int height;
    private int samplesPerPixel;
    private int[] bitsPerSample;
    private Optional<String> directoryName;

    public ImageMetadata(int width, int height, int samplesPerPixel, int[] bitsPerSample,
            Optional<String> directoryName) {
        this.width = width;
        this.height = height;
        this.samplesPerPixel = samplesPerPixel;
        this.bitsPerSample = bitsPerSample;
        this.directoryName = directoryName;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSamplesPerPixel() {
        return samplesPerPixel;
    }

    public int[] getBitsPerSample() {
        return bitsPerSample;
    }

    public int getTotalBitsPerPixel() {
        int totalBits = 0;
        for (int bits : bitsPerSample) {
            totalBits += bits;
        }
        return totalBits;
    }

    public Optional<String> getDirectoryName() {
        return directoryName;
    }
}