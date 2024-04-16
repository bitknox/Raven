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

	@Parameter(names = { "-log", "-verbose" }, description = "Level of verbosity")
	public Logger.LogLevel verbose = Logger.LogLevel.WARNING;

	@Parameter(names = { "-ir", "--input-raster" }, description = "Input raster file", required = true)
	public String inputRaster = null;

	@Parameter(names = { "-iv", "--input-vector" }, description = "Input vector file", required = true)
	public String inputVector = null;

	@Parameter(names = { "-o", "--output" }, description = "Optional join visualization output file")
	public String outputPath = null;

	@Parameter(names = { "-r",
			"--filter-ranges" }, description = "Filter function. If one range is given it will filter based on the packed value. If a range is given for every sample, it will accept only values that fall within the range for all samples. Min/max value for each range should be separated by a hyphen, different ranges should be separated by either a comma of a space", splitter = RangeSplitter.class)
	public List<Long> ranges = Arrays.asList((long) Integer.MIN_VALUE, (long) Integer.MAX_VALUE);

	@Parameter(names = { "-t", "--tile-size" }, description = "Size of the raster image tiles")
	public int tileSize = 2048;

	@Parameter(names = { "-p",
			"--parallel" }, description = "Run the join in parallel (requires running as a stream)", arity = 1)
	public boolean parallel = true;

	@Parameter(names = { "-s",
			"--stream" }, description = "Run the join as a stream. This improves the memory usage of the program", arity = 1)
	public boolean streamed = true;

	@Parameter(names = { "-c",
			"--cache" }, description = "Use cached raster structures", arity = 1)
	public boolean isCaching = true;

	@Parameter(names = { "-cv",
			"--crop-to-vector" }, description = "Crop the output image so it only shows the part of the raster data that lies within the minimum bounding rectangle of the given vector data", arity = 1)
	public boolean cropToVector = false;

	@Parameter(names = { "--k-size" }, description = "size of k in the k2-raster algorithm")
	public int kSize = 2;

	@Parameter(names = { "--r-tree-min-children" }, description = "Minimum number of children in the R-tree")
	public int rTreeMinChildren = 4;

	@Parameter(names = { "--r-tree-max-children" }, description = "Maximum number of children in the R-tree")
	public int rTreeMaxChildren = 8;

	@Parameter(names = { "--dac-fraction-size" }, description = "Size of the fraction used in the DAC algorithm")
	public int dacFractionSize = 20;
}