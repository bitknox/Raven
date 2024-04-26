# Eagle (Efficient Analysis of Geospatial joins with Large-data Evaluations)

Benchmark framework for running, evaluating, and plotting results for geospatial systems.

## Prerequisite

- golang (minimum 1.20)
- docker
- python 3

## Usage

```bash
go run main.go
```

## Input example

```json
{
    "name": "BenchmarkSuite",
    "ylimit": 1500, // this determines the maximum y value shown in the plots (may be omitted if it should be determined automatically)
    "benchmarks": [
        {
            "name": ["Benchmark1","Benchmark2"], // multiple benchmarks can be defined in one by having a list of names and lists where arguments should be different
            "group": "Group1", // it is also possible to have only 1 name but multiple groups. In the resulting plot, data in the same group will be next to eachter and data with the same name will have the same colour (defined in the outer object).
            "iterations": 10, // if only one value is given, it will be used by all benchmarks created by this (in this case, Benchmark1 and Benchmark2)
            "command": {
                "path": "cmd",
                "args": [
                    ["/c","/d"], // note that multiple values can also be specified within the args list using the same rules as described above
                    "dir"
                ]
            },
            "environment_options": {
                "environment_type": "local"
            }
        },
        {
            "name": "Benchmark3",
            "iterations": 20,
            "group": "Group2",
            "command": {
                "path": "ls",
                "args": ["-la"]
            },
                "environment_options": {
                "environment_type": "docker",
                "docker_file_path": "./test/", //The directory containing a file named Dockerfile
                "docker_mount_path": "./dir"
            }
        }
    ],
    "colours": { // The colours should be supported by matplotlib
        "Benchmark1": "red",
        "Benchmark2": "green",
        "Benchmark3": "blue"
    }
}
```

### NOTE:
- the pair of (name, group) should be unique for all tests within a suite
- the length of any list of arguments must either be 1 or match the length of the name or group list.
- the name or group lists must also match unless one of them has length 1, in which case the length of the other will be used

## Common execution interface

Programs executed by the benchmarking tool should use the following input/output interfaces:

```go

/**
* Input arguments
*/

args := []string{"inputVectorPath", "inputRasterPath", "...args", "iterations"}


/**
* Output interface (JSON)
*/
type BenchmarkResult struct {
	// The name of the benchmark
	Name string `json:"name"`
	// The colour used in the bar chart
	Group string `json:"group"`
	// The time it took to run the benchmark in milliseconds
	Times []float64 `json:"times"`
	// The number of iterations
	Iterations int `json:"iterations"`
	// Labels to add to the plot
	Labels []string `json:"labels"`
}

```

## Plotter
See [plotter](./plotter/plotter.py) for source-code
### Usage

```bash
python plotter/plotter.py
```

```bash
usage: plotter.py [-h] [-i INPUT] [-o OUTPUT] [-id IDENTIFIER] [-sp] [-ylim Y_LIMIT] [-g GROUPS] [-sg]

plots results of benchmarks as a bar chart showing average running times, as well as separate charts showing the progressing of running time of each experiment over time.

options:
  -h, --help            show this help message and exit
  -i INPUT, --input INPUT
                        a path to the result JSON file to plot
  -o OUTPUT, --output OUTPUT
                        a path to the folder the images should be placed in
  -id IDENTIFIER, --identifier IDENTIFIER
                        an identifier added to the end of the file to prevent overriding
  -sp, --sub-plots      whether a separate plot should be made for each experiment showing how the running time evolved over time
  -ylim Y_LIMIT, --y-limit Y_LIMIT
                        the top y-limit of the produced plots
  -sg, --split-groups   generate separate bar-charts for the different groups. If not given, one bar-chart will be generated containing all groups.
```