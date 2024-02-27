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
   "docker_file_path": "./test/Dockerfile" //only required if using docker
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

args := []string{"inputVectorPath", "inputRasterPath", "iterations"}


/**
* Output interface (JSON)
*/
type BenchmarkResult struct {
 // The name of the benchmark
 Name string `json:"name"`
 // The time it took to run the benchmark (milliseconds)
 Time float64 `json:"time"`
 // The number of iterations
 Iterations int `json:"iterations"`
 // The number of bytes processed
}

```
