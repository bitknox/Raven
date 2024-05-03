package dk.itu.raven.visualizer;

import java.awt.Color;

/**
 * Class used to pass options to the visualizer.
 */
public class VisualizerOptions {
	public String outputPath, outputFormat;
	public boolean useOutput, cropToVector, drawFeatures, useOriginalColours;
	public Color primaryColor, secondaryColor, trinaryColor, background;

	public VisualizerOptions(Color primaryColor, String outputPath, String outputFormat,
			boolean useOutput, boolean cropToVector, Color background, boolean drawFeatures, Color secondaryColor,
			Color trinaryColor, boolean useOriginalColours) {
		this.primaryColor = primaryColor;
		this.outputPath = outputPath;
		this.outputFormat = outputFormat;
		this.useOutput = useOutput;
		this.cropToVector = cropToVector;
		this.background = background;
		this.drawFeatures = drawFeatures;
		this.secondaryColor = secondaryColor;
		this.trinaryColor = trinaryColor;
		this.useOriginalColours = useOriginalColours;
	}

}
