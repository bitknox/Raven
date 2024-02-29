# Benchmark framework

Benchmark framework for running evaluating geospatial systems

## Prerequisite

- golang
- docker

## Usage

```bash
go run main.go
```

## Input example

```json
[
 {
  "name": "Benchmark1",
  "iterations": 1,
  "command": {
   "path": "cmd",
   "args": ["/c", "dir"]
  },
  "environment_options": {
   "environment_type": "local"
  }
 },
 {
  "name": "Benchmark2",
  "iterations": 1,
  "command": {
   "path": "ls",
   "args": ["-la"]
  },
  "environment_options": {
   "environment_type": "docker",
   "docker_file_path": "./test/Dockerfile", //only required if using docker
   "docker_mount_path": "./dir"
  }
 }
]
```

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
 // The time it took to run the benchmark in milliseconds
 Times []float64 `json:"times"`
 // The number of iterations
 Iterations int `json:"iterations"`
 // The datasets used for the benchmark
 Datsets []string `json:"datasets"`
 // Labels to add to the plot
 Labels []string `json:"labels"`
}

```
