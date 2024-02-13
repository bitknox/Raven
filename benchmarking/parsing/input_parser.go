package parsing

import (
	"encoding/json"
	"os"

	"github.com/bitknox/Raven/benchmarking/model"
)

func ParseInput(jsonPath string) ([]*model.Benchmark, error) {
	file, err := os.ReadFile(jsonPath)
	if err != nil {
		return nil, err
	}

	var benchmarks []*model.Benchmark
	err = json.Unmarshal(file, &benchmarks)
	if err != nil {
		return nil, err
	}

	return benchmarks, nil
}
