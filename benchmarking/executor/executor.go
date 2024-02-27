package executor

import (
	"fmt"

	"github.com/bitknox/Raven/benchmarking/environments"
	"github.com/bitknox/Raven/benchmarking/model"
	"github.com/bitknox/Raven/benchmarking/parsing"
)

func ExecuteBenchmark(benchmark *model.Benchmark) (*model.BenchmarkResult, error) {
	env := environments.NewEnvironment(benchmark.EnvironmentOptions)

	//append iterations to command arguments
	benchmark.Command.Args = append(benchmark.Command.Args, fmt.Sprintf("%d", benchmark.Iterations))

	env.Runner.Setup()
	r, err := env.Runner.RunCommand(benchmark.Command)

	if err != nil {
		fmt.Printf("Failed to run command: %s\n", benchmark.Command)
		return nil, err
	}

	defer env.Runner.Teardown()
	//generate result
	result, err := parsing.ParseResult(r)

	result.Name = benchmark.Name
	result.Iterations = benchmark.Iterations

	if err != nil {
		fmt.Println("Failed to parse result")
		return nil, err
	}

	isValid := parsing.ResultIsValid(result)

	if !isValid {
		return nil, fmt.Errorf("benchmark result was not valid: %s", benchmark.Name)
	}

	return result, nil

}

func ExecuteBenchmarks(benchmarks []*model.Benchmark) []*model.BenchmarkResult {
	var results []*model.BenchmarkResult
	for _, benchmark := range benchmarks {
		benchResult, err := ExecuteBenchmark(benchmark)

		if err != nil {
			fmt.Println(err)
			continue
		}
		results = append(results, benchResult)
	}
	return results
}
