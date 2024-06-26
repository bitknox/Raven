package dk.itu.raven.runner;

import java.util.List;

import com.beust.jcommander.Parameter;

import dk.itu.raven.io.commandline.ResultType;

public class CommandLineArgs {
        @Parameter(names = { "--help", "-h" }, help = true)
	public boolean help = false;
        
        @Parameter(names = { "-ir", "--input-raster" }, description = "Input raster file", required = true)
        public String inputRaster = null;

        @Parameter(names = { "-iv", "--input-vector" }, description = "Input vector file", required = true)
        public String inputVector = null;

        @Parameter(names = { "-fl",
                        "--filter-low" }, description = "Lower value for the filter range", splitter = RangeSplitter.class)
        public List<Long> filterLow = null;

        @Parameter(names = { "-fh",
                        "--filter-high" }, description = "Upper value for the filter range", splitter = RangeSplitter.class)
        public List<Long> filterHigh = null;

        @Parameter(names = { "-ts",
                        "--tile-size" }, description = "The size of the tiles (only used if the join is streamed)")
        public int tileSize = 2048;

        @Parameter(names = { "-t",
                        "--join-type" }, description = "The type of join to perform", required = true)
        public JoinType joinType = null;

        @Parameter(names = { "-c",
                        "--cached" }, description = "Whether to use cached structures", arity = 1)
        public boolean cached = true;

        @Parameter(names = { "-cl",
                        "--cache-location" }, description = "The parent directory to use for caches")
        public String cacheDir = ".";

        @Parameter(names = { "-i",
                        "--iterations" }, description = "The number of tests performed using these settings", required = true)
        public long iterations = -1;

        @Parameter(names = { "--k-size" }, description = "size of k in the k2-raster algorithm")
        public int kSize = 2;

        @Parameter(names = { "--r-tree-min-children" }, description = "Minimum number of children in the R-tree")
        public int rTreeMinChildren = 1;

        @Parameter(names = { "--r-tree-max-children" }, description = "Maximum number of children in the R-tree")
        public int rTreeMaxChildren = 8;

        @Parameter(names = { "--dac-fraction-size" }, description = "Size of the fraction used in the DAC algorithm")
        public int dacFractionSize = 20;

        @Parameter(names = { "-rt", "--result-type" }, description = "The type of result to use")
        public ResultType resultType = ResultType.RANGEVALUE;

}
