package model

import (
	"encoding/json"
	"fmt"

	"github.com/bitknox/Raven/benchmarking/environments"
)

type InputSuite struct {
	Benchmarks []*Input `json:"benchmarks"`
	Name       string   `json:"name"`
	YLimit     int      `json:"y_limit"`
}

type Input struct {
	// The name of the benchmark
	Name []string `json:"name"`
	// The number of iterations
	Iterations SingleOrList[int] `json:"iterations"`

	Colour SingleOrList[string] `json:"colour"`
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
	switch b[0] {
	case '[':
		return a.unmarshalMany(b)
	default:
		return a.unmarshalSingle(b)
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
