package dk.itu.raven.visualizer;

import java.awt.Color;

public class VisualizerOptionsBuilder {
	private String outputPath = "./output.tif";
	private String outputFormat = "tif";
	private boolean useRandomColor = false;
	private boolean useOutput = false;
	private boolean cropToVector = true;
	private boolean drawFeatures = true;
	private Color background = Color.WHITE;
	private Color primaryColor = Color.black;
	private Color secondaryColor = Color.RED;
	private Color ternaryColor = Color.GREEN;

	public VisualizerOptionsBuilder setOutputPath(String outputPath) {
		this.outputPath = outputPath;
		return this;
	}

	public VisualizerOptionsBuilder setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
		return this;
	}

	public VisualizerOptionsBuilder setUseRandomColor(boolean useRandomColor) {
		this.useRandomColor = useRandomColor;
		return this;
	}

	public VisualizerOptionsBuilder setUseOutput(boolean useOutput) {
		this.useOutput = useOutput;
		return this;
	}

	public VisualizerOptionsBuilder setPrimaryColor(Color color) {
		this.primaryColor = color;
		return this;
	}

	public VisualizerOptionsBuilder setBackground(Color color) {
		this.background = color;
		return this;
	}

	public VisualizerOptionsBuilder setCropToVector(boolean cropToVector) {
		this.cropToVector = cropToVector;
		return this;
	}

	public VisualizerOptionsBuilder setDrawFeatures(boolean drawFeatures) {
		this.drawFeatures = drawFeatures;
		return this;
	}

	public VisualizerOptionsBuilder setSecondaryColor(Color secondaryColor) {
		this.secondaryColor = secondaryColor;
		return this;
	}

	public VisualizerOptionsBuilder setTernaryColor(Color ternaryColor) {
		this.ternaryColor = ternaryColor;
		return this;
	}

	public VisualizerOptions build() {
		return new VisualizerOptions(primaryColor, outputPath, outputFormat, useRandomColor, useOutput, cropToVector,
				background, drawFeatures, secondaryColor, ternaryColor);
	}
}