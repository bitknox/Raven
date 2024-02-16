package executor

import (
	"fmt"

	"github.com/bitknox/Raven/benchmarking/environments"
	"github.com/bitknox/Raven/benchmarking/model"
	"github.com/bitknox/Raven/benchmarking/parsing"
)

func ExecuteBenchmark(benchmark *model.Benchmark) (*model.BenchmarkResult, error) {
	env := environments.NewEnvironment(benchmark.EnvironmentOptions)
	env.Runner.Setup()
	r, err := env.Runner.RunCommand(benchmark.Command)

	fmt.Println(r)
	if err != nil {
		return nil, err
	}
	env.Runner.Teardown()
	//generate result
	result, err := parsing.ParseResult(r)

	if err != nil {
		return nil, err
	}

	isValid := parsing.ResultIsValid(result)

	if !isValid {
		return nil, fmt.Errorf("benchmar result was not valid: %s", benchmark.Name)
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
