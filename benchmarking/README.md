# Benchmark framework

Benchmark framework for running evaluating geospatial systems

## Prequsites

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
