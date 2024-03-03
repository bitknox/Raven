package dk.itu.raven.visualizer;

import java.awt.Color;

/**
 * Class used to pass options to the visualizer.
 */
public class VisualizerOptions {
	public String outputPath, outputFormat;
	public boolean useRandomColor, useOutput, cropToVector, drawFeatures;
	public Color primaryColor, secondaryColor, ternaryColor, background;

	public VisualizerOptions(Color primaryColor, String outputPath, String outputFormat, boolean useRandomColor,
			boolean useOutput, boolean cropToVector, Color background, boolean drawFeatures, Color secondaryColor,
			Color ternaryColor) {
		this.primaryColor = primaryColor;
		this.outputPath = outputPath;
		this.outputFormat = outputFormat;
		this.useRandomColor = useRandomColor; // TODO: not currently used
		this.useOutput = useOutput;
		this.cropToVector = cropToVector;
		this.background = background;
		this.drawFeatures = drawFeatures;
		this.secondaryColor = secondaryColor;
		this.ternaryColor = ternaryColor;
	}

}
