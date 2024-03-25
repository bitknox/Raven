package model

import "github.com/bitknox/Raven/benchmarking/environments"

type BenchmarkSuiteResult struct {
	Results []*BenchmarkResult `json:"data"`
	Name    string             `json:"title"`
}

type BenchmarkSuite struct {
	Benchmarks []*Benchmark `json:"benchmarks"`
	Name       string       `json:"name"`
}

type BenchmarkResult struct {
	// The name of the benchmark
	Name string `json:"name"`
	// The time it took to run the benchmark in milliseconds
	Times []float64 `json:"times"`
	// The number of iterations
	Iterations int `json:"iterations"`
	// Labels to add to the plot
	Labels []string `json:"labels"`
}

type Benchmark struct {
	// The name of the benchmark
	Name string `json:"name"`
	// The number of iterations
	Iterations int `json:"iterations"`
	// The command to run
	Command environments.Command `json:"command"`
	//optional values for the environment
	EnvironmentOptions environments.EnvironmentOptions `json:"environment_options"`
}
