package dk.itu.raven.io;

/**
 * ImageMetadata
 */
public class ImageMetadata {

    private int width;
    private int height;
    private int samplesPerPixel;
    private int[] bitsPerSample;

    public ImageMetadata(int width, int height, int samplesPerPixel, int[] bitsPerSample) {
        this.width = width;
        this.height = height;
        this.samplesPerPixel = samplesPerPixel;
        this.bitsPerSample = bitsPerSample;
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
}