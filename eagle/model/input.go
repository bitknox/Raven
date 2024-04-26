package model

import (
	"encoding/json"
	"fmt"

	"github.com/bitknox/Raven/benchmarking/environments"
)

type InputSuite struct {
	Benchmarks []*Input          `json:"benchmarks"`
	Name       string            `json:"name"`
	YLimit     int               `json:"y_limit"`
	Colours    map[string]string `json:"colours"`
}

type Input struct {
	// The name of the benchmark
	Name SingleOrList[string] `json:"name"`
	// The number of iterations
	Iterations SingleOrList[int] `json:"iterations"`
	// The colour used in the bar chart
	Group SingleOrList[string] `json:"group"`
	// The command to run
	Command InputCommand `json:"command"`
	//optional values for the environment
	EnvironmentOptions environments.EnvironmentOptions `json:"environment_options"`
}

type SingleOrList[T any] struct {
	List []T
}

type InputCommand struct {
	Path string                 `json:"path"`
	Args []SingleOrList[string] `json:"args"`
}

func (a *SingleOrList[T]) UnmarshalJSON(b []byte) error {
	if len(b) == 0 {
		return fmt.Errorf("no bytes to unmarshal")
	}
	if b[0] == '[' {
		return a.unmarshalMany(b)
	} else {
		return a.unmarshalSingle(b) // we assume this will be a correct type
	}
}

func (a *SingleOrList[T]) unmarshalSingle(b []byte) error {
	var t T
	err := json.Unmarshal(b, &t)
	if err != nil {
		return err
	}
	a.List = []T{t}
	return nil
}

func (a *SingleOrList[T]) unmarshalMany(b []byte) error {
	err := json.Unmarshal(b, &a.List)
	if err != nil {
		return err
	}
	return nil
}
