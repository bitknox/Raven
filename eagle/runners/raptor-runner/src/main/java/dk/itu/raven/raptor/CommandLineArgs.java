package dk.itu.raven.raptor;

import com.beust.jcommander.Parameter;

public class CommandLineArgs {
        @Parameter(names = { "--help", "-h" }, help = true)
	public boolean help = false;
        
        @Parameter(names = { "-ir", "--input-raster" }, description = "Input raster file", required = true)
        public String inputRaster = null;

        @Parameter(names = { "-iv", "--input-vector" }, description = "Input vector file", required = true)
        public String inputVector = null;

        @Parameter(names = { "-fl",
                        "--filter-low" }, description = "Lower value for the filter range")
        public long filterLow = (long) Integer.MIN_VALUE;

        @Parameter(names = { "-fh",
                        "--filter-high" }, description = "Upper value for the filter range")
        public long filterHigh = (long) Integer.MAX_VALUE;

        @Parameter(names = { "-i",
                        "--iterations" }, description = "The number of tests performed using these settings", required = true)
        public long iterations = -1;

        @Parameter(names = { "-p",
                        "--parallel" }, description = "Run the join in parallel (requires running as a stream)", arity = 1)
        public boolean parallel = true;
}
