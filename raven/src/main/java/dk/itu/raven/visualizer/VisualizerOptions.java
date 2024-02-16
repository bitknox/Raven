package dk.itu.raven.visualizer;

import java.awt.Color;

/**
 * Class used to pass options to the visualizer.
 */
public class VisualizerOptions {
	public String outputPath, outputFormat;
	public boolean useRandomColor, useOutput;
	public Color color;

	public VisualizerOptions(Color color, String outputPath, String outputFormat, boolean useRandomColor,
			boolean useOutput) {
		this.color = color;
		this.outputPath = outputPath;
		this.outputFormat = outputFormat;
		this.useRandomColor = useRandomColor;
		this.useOutput = useOutput;
	}

}
