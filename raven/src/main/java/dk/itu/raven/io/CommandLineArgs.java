package dk.itu.raven.io;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

public class CommandLineArgs {
	@Parameter
	public List<String> parameters = new ArrayList<>();

	@Parameter(names = { "--help", "-h" }, help = true)
	public boolean help = false;

	@Parameter(names = { "-log", "-verbose" }, description = "Level of verbosity")
	public boolean verbose = false;

	@Parameter(names = { "-ir", "--input-raster" }, description = "Input raster file", required = true)
	public String inputRaster = null;

	@Parameter(names = { "-iv", "--input-vector" }, description = "Input vector file", required = true)
	public String inputVector = null;

	@Parameter(names = { "-o", "--output" }, description = "Optional join visualization output file")
	public String outputPath = null;

	@Parameter(names = { "-min", "--min-value" }, description = "Minimum filter value")
	public Integer minValue = Integer.MIN_VALUE;

	@Parameter(names = { "-max", "--max-filter" }, description = "Maximum filter value")
	public Integer maxValue = Integer.MAX_VALUE;

}