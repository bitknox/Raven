# Raptor
Based on the [RaptorJoin algorithm](https://dl.acm.org/doi/pdf/10.1145/3474717.3483971), built on the implementation by [Beast](https://bitbucket.org/bdlabucr/beast/src/master/).

## Prerequisite

- maven
- jdk 16

## Usage

```bash
java -jar ./target/raptor-${version}.jar

Usage: Raptor [options]
  Options:
    -r, --filter-ranges
      Filter function. If one range is given it will filter based on the
      packed value. If a range is given for every sample, it will accept only
      values that fall within the range for all samples. Min/max value for
      each range should be separated by a hyphen, different ranges should be
      separated by either a comma of a space
      Default: []
    --help, -h

  * -ir, --input-raster
      Input raster file
  * -iv, --input-vector
      Input vector file
    -p, --parallel
      Run the join in parallel
      Default: true
```

## Building

```bash
mvn package -DskipTests
```