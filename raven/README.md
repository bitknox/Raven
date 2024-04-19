# Raven (RAster VEctor joiN)

Raven is a system build around the Raven Join algorithm. The algorithm is largely based on work done by Susana Ladra et-al (found [here](https://www.sciencedirect.com/science/article/pii/S0306437916306214?fr=RR-2&ref=pdf_download&rr=876cbe2d3c0c92ca)), with the implementation and various modifications done by us.

## Usage

```bash
java -jar ./target/raven-${version}.jar
Usage: Raven [options]
  Options:
    -c, --cache
      Use cached raster structures
      Default: true
    -cv, --crop-to-vector
      Crop the output image so it only shows the part of the raster data that
      lies within the minimum bounding rectangle of the given vector data
      Default: false
    -r, --filter-ranges
      Filter function. If one range is given it will filter based on the
      packed value. If a range is given for every sample, it will accept only
      values that fall within the range for all samples. Min/max value for
      each range should be separated by a hyphen, different ranges should be
      separated by either a comma of a space
      Default: [-2147483648, 2147483647]
    --help, -h

  * -ir, --input-raster
      Input raster file (geotiff or tiff + tfw)
  * -iv, --input-vector
      Input vector file (shapefile)
    -o, --output
      Optional join visualization output file
    -p, --parallel
      Run the join in parallel (requires running as a stream)
      Default: true
    -s, --stream
      Run the join as a stream. This improves the memory usage of the program
      Default: true
    -t, --tile-size
      Size of the raster image tiles
      Default: 2048
    -log, -verbose
      Level of verbosity
      Default: WARNING
      Possible Values: [NONE, ERROR, WARNING, INFO, DEBUG]
```