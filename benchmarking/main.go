package main

import (
	"fmt"

	"github.com/bitknox/Raven/benchmarking/environments"
	"github.com/bitknox/Raven/benchmarking/parsing"
)

func main() {
	model, err := parsing.ParseInput("./test/test_input.json")

	if err != nil {
		fmt.Println(err)
		return
	}

	for _, benchmark := range model {
		env := environments.NewEnvironment(benchmark.EnvironmentOptions)
		env.Runner.Setup()
		r, err := env.Runner.RunCommand(benchmark.Command)
		fmt.Println(r)
		if err != nil {
			fmt.Println(err)
		}
		env.Runner.Teardown()
	}
}
