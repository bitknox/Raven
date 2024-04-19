# Master Thesis - Performant Vector/Raster join (Raven)

Raven is a system for computing spatial joins with raster and vector data efficiently. A benchmarking component is included for evaluating Raven along with other state-of-the-art systems with raster/vector join capabilities.

## Description

- Input Reader: Read and partition multiple data formats in a parallel manner.
- Data Visualizer: Enables users to visualize join results & data sets.
- RavenJoin Algorithm: The core spatial join algorithm, introduces key enhancements such as the use of an R*-tree for faster query times, compressed cached raster data structures & parallel computation.

## Overview
- [Raven](./raven/) is the main system, built around the Raven Join algorithm for computing raster vector joins.
- [Raptor](./raptor/) is an implementation of the RaptorJoin algorithm outside Spark. Most code used in this component is from [Beast](https://bitbucket.org/bdlabucr/beast/src/master/).
- [Swift](./swift/) is a program for generating synthetic vector and raster data. It can generate shapefiles for vector data and geotiff images using one of 2 types of noise for raster data.
- [Eagle](./eagle/) is a program for benchmarking programs, used mainly to compare different spatial join implementations. It supports both running experiments and plotting the results of these.

## Getting Started

### Dependencies

- maven
- jdk 16 (Raven)
- docker (Eagle)
- golang (Eagle)
- Python (Eagle)

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


## Authors

[Alexander Bilde Pedersen](https://github.com/Burdmann)

[Benjamin Thygesen](https://github.com/Mansin-ITU)

[Johan Flensmark](https://github.com/bitknox)

## License

This project is licensed under the Apache License License - see the LICENSE.md file for details
