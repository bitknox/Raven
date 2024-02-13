package main

import (
	"fmt"

	"github.com/bitknox/Raven/benchmarking/environments"
)

func main() {
	enviroment := environments.NewEnvironment(environments.EnvironmentOptions{
		EnvironmentType: environments.Docker,
		DockerFilePath:  "./test/Dockerfile",
	})

	err := enviroment.Runner.Setup()
	if err != nil {
		panic(err)
	}

	res, err := enviroment.Runner.RunCommand(environments.Command{
		Path: "cmd",
		Args: []string{"/C", "dir"},
	})

	fmt.Println(res)

	if err != nil {
		panic(err)
	}
	enviroment.Runner.Teardown()

}
