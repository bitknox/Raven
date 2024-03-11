# Master Thesis - Performant Vector/Raster join (Raven)

Raven is a system for computing spatial joins with raster and vector data efficiently. A benchmarking component is included for evaluating Raven along with other state-of-the-art systems with raster/vector join capabilities.

## Description

- Input Reader: Read and partition multiple data formats in a parallel manner.
- Data Visualizer: Enables users to visualize join results & data sets.
- RavenJoin Algorithm: The core spatial join algorithm, introduces key enhancements such as the use of an R*-tree for faster query times, compressed cached raster data structures & parallel computation.

## Getting Started

### Dependencies

- maven
- jdk 16
- docker (benchmarking)
- golang (benchmarking)

### Building

#### Building Raven

```bash
cd raven
mvn package -DskipTests
```

#### Building Benchmark tool

```bash
cd benchmarking
go build
```

### Executing program

#### Executing raven

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

#### Running the benchmarking tool

See: [benchmark](./benchmarking/README.md)

## Authors

[Alexander Bilde Pedersen](https://github.com/Burdmann)

[Benjamin Thygesen](https://github.com/Mansin-ITU)

[Johan Flensmark](https://github.com/bitknox)

## License

This project is licensed under the Apache License License - see the LICENSE.md file for details
