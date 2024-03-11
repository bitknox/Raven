package dk.itu.raptor.io.commandline;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

public class CommandLineArgs {
        @Parameter
        public List<String> parameters = new ArrayList<>();

        @Parameter(names = { "--help", "-h" }, help = true)
        public boolean help = false;

        @Parameter(names = { "-ir", "--input-raster" }, description = "Input raster file", required = true)
        public String inputRaster = null;

        @Parameter(names = { "-iv", "--input-vector" }, description = "Input vector file", required = true)
        public String inputVector = null;

        @Parameter(names = { "-r",
                        "--filter-ranges" }, description = "Filter function. If one range is given it will filter based on the packed value. If a range is given for every sample, it will accept only values that fall within the range for all samples. Min/max value for each range should be separated by a hyphen, different ranges should be separated by either a comma of a space", splitter = RangeSplitter.class)
        public List<Long> ranges = new ArrayList<>();

        @Parameter(names = { "-p",
                        "--parallel" }, description = "Run the join in parallel (requires running as a stream)", arity = 1)
        public boolean parallel = true;
}