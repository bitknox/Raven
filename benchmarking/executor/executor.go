package executor

import (
	"fmt"
	"strings"

	"github.com/bitknox/Raven/benchmarking/environments"
	"github.com/bitknox/Raven/benchmarking/model"
	"github.com/bitknox/Raven/benchmarking/parsing"
)

func ExecuteBenchmark(benchmark *model.Benchmark) (*model.BenchmarkResult, error) {
	env := environments.NewEnvironment(benchmark.EnvironmentOptions)

	//append iterations to command arguments
	benchmark.Command.Args = append(benchmark.Command.Args, "\"-i\"", fmt.Sprintf("%d", benchmark.Iterations))

	setupErr := env.Runner.Setup()

	if setupErr != nil {
		fmt.Println("Failed to setup environment, exiting")
		return nil, setupErr
	}

	r, err := env.Runner.RunCommand(benchmark.Command)

	if err != nil {
		fmt.Printf("Failed to run command: %s exiting... \n", benchmark.Command)
		return nil, err
	}

	split := strings.Split(r, "\n")
	// the output always ends with an empty line,
	// therefore the line we are interested in is the second to last one
	r = split[len(split)-2]

	fmt.Println(r)
	//get last line of output

	defer env.Runner.Teardown()
	//generate result
	result, err := parsing.ParseResult(r)

	if err != nil {
		fmt.Println("Failed to parse result, exiting...")
		return nil, err
	}

	result.Name = benchmark.Name
	result.Iterations = benchmark.Iterations

	isValid := parsing.ResultIsValid(result)

	if !isValid {

		return nil, fmt.Errorf("benchmark result was not valid: %s... Exiting", benchmark.Name)
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
