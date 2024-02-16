package dk.itu.raven.io.commandline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.Parameter;

import java.util.Arrays;
import java.util.List;

import dk.itu.raven.util.Logger;

public class CommandLineArgs {
	@Parameter
	public List<String> parameters = new ArrayList<>();

	@Parameter(names = { "--help", "-h" }, help = true)
	public boolean help = false;

	@Parameter(names = { "-log", "-verbose" }, description = "Level of verbosity", validateWith = {LogLevelValidator.class})
	public Logger.LogLevel verbose = Logger.LogLevel.WARNING;

	@Parameter(names = { "-ir", "--input-raster" }, description = "Input raster file", required = true)
	public String inputRaster = null;

	@Parameter(names = { "-iv", "--input-vector" }, description = "Input vector file", required = true)
	public String inputVector = null;

	@Parameter(names = { "-o", "--output" }, description = "Optional join visualization output file")
	public String outputPath = null;

	@Parameter(names = {"-ranges","--filter-ranges"}, description = "", splitter = RangeSplitter.class)
	public List<Integer> ranges = Arrays.asList(Integer.MIN_VALUE,Integer.MAX_VALUE);

	// @Parameter(names = { "-min", "--min-value" }, description = "Minimum filter value")
	// public Integer minValue = Integer.MIN_VALUE;

	// @Parameter(names = { "-max", "--max-filter" }, description = "Maximum filter value")
	// public Integer maxValue = Integer.MAX_VALUE;
}