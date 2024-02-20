package dk.itu.raven.io.commandline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.Parameter;

import dk.itu.raven.util.Logger;

public class CommandLineArgs {
	@Parameter
	public List<String> parameters = new ArrayList<>();

	@Parameter(names = { "--help", "-h" }, help = true)
	public boolean help = false;

	@Parameter(names = { "-log", "-verbose" }, description = "Level of verbosity", validateWith = {
			LogLevelValidator.class })
	public Logger.LogLevel verbose = Logger.LogLevel.WARNING;

	@Parameter(names = { "-ir", "--input-raster" }, description = "Input raster file", required = true)
	public String inputRaster = null;

	@Parameter(names = { "-iv", "--input-vector" }, description = "Input vector file", required = true)
	public String inputVector = null;

	@Parameter(names = { "-o", "--output" }, description = "Optional join visualization output file")
	public String outputPath = null;

	@Parameter(names = { "-ranges",
			"--filter-ranges" }, description = "Filter function. If one range is given it will filter based on the packed value. If a range is given for every sample, it will accept only values that fall within the range for all samples", splitter = RangeSplitter.class)
	public List<Long> ranges = Arrays.asList((long) Integer.MIN_VALUE, (long) Integer.MAX_VALUE);

	@Parameter(names = { "-tile", "-tile-size" }, description = "Size of the raster image tiles")
	public int tileSize = 2048;
}