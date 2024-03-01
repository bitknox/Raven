package dk.itu.raven.visualizer;

import java.awt.Color;

public class VisualizerOptionsBuilder {
	private String outputPath = "./output.tif";
	private String outputFormat = "tif";
	private boolean useRandomColor = false;
	private boolean useOutput = false;
	private boolean cropToVector = true;
	private Color color = Color.black;

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

	public VisualizerOptionsBuilder setColor(Color color) {
		this.color = color;
		return this;
	}

	public VisualizerOptionsBuilder setCropToVector(boolean cropToVector) {
		this.cropToVector = cropToVector;
		return this;
	}

	public VisualizerOptions build() {
		return new VisualizerOptions(color, outputPath, outputFormat, useRandomColor, useOutput, cropToVector);
	}
}