package model

import (
	"fmt"

	"github.com/bitknox/Raven/benchmarking/environments"
)

type BenchmarkSuiteResult struct {
	Results []*BenchmarkResult `json:"data"`
	Name    string             `json:"title"`
	Colours map[string]string  `json:"colours"`
}

type BenchmarkSuite struct {
	Benchmarks []*Benchmark      `json:"benchmarks"`
	Name       string            `json:"name"`
	YLimit     int               `json:"y_limit"`
	Colours    map[string]string `json:"colours"`
}

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

type Benchmark struct {
	// The name of the benchmark
	Name string `json:"name"`
	// The number of iterations
	Iterations int `json:"iterations"`
	//The colour of the benchmark
	Group string `json:"group"`
	// The command to run
	Command environments.Command `json:"command"`
	//optional values for the environment
	EnvironmentOptions environments.EnvironmentOptions `json:"environment_options"`
}

func makeArguments[T any](args []SingleOrList[T], idx int) []T {
	var res = make([]T, len(args))
	for i, arg := range args {
		res[i] = arg.List[idx]
	}
	return res
}

func extendList[T any](list *SingleOrList[T], length int) error {
	if len(list.List) == 1 {
		val := list.List[0]
		list.List = make([]T, 0)
		for i := 0; i < length; i++ {
			list.List = append(list.List, val)
		}
	} else if len(list.List) != length {
		return fmt.Errorf("argument has a different length than the names list")
	}
	return nil
}

func FromInputBenchmark(inputBenchmark *Input) []*Benchmark {
	benchmarks := make([]*Benchmark, len(inputBenchmark.Name))
	for i, name := range inputBenchmark.Name {
		var length = len(inputBenchmark.Name)
		for j := 0; j < len(inputBenchmark.Command.Args); j++ {
			arg := &inputBenchmark.Command.Args[j]
			err := extendList(arg, length)
			if err != nil {
				panic(fmt.Sprintf("%s: %v", err, arg.List))
			}
		}

		err := extendList(&inputBenchmark.Iterations, length)
		if err != nil {
			panic(fmt.Sprintf("%s: iterations (%v)", err, inputBenchmark.Iterations.List))
		}

		err = extendList(&inputBenchmark.Group, length)
		if err != nil {
			panic(fmt.Sprintf("%s: colour (%v)", err, inputBenchmark.Group.List))
		}

		benchmarks[i] = &Benchmark{
			Name:       name,
			Iterations: inputBenchmark.Iterations.List[i],
			Group:      inputBenchmark.Group.List[i],
			Command: environments.Command{
				Path: inputBenchmark.Command.Path,
				Args: makeArguments(inputBenchmark.Command.Args, i),
			},
			EnvironmentOptions: inputBenchmark.EnvironmentOptions,
		}
	}
	return benchmarks
}

func FromInputSuite(inputSuite *InputSuite) *BenchmarkSuite {
	benchmarks := make([]*Benchmark, 0)
	for _, benchmark := range inputSuite.Benchmarks {
		benchmarks = append(benchmarks, FromInputBenchmark(benchmark)...)
	}
	return &BenchmarkSuite{
		Benchmarks: benchmarks,
		Name:       inputSuite.Name,
		YLimit:     inputSuite.YLimit,
		Colours:    inputSuite.Colours,
	}
}
