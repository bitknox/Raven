package parsing

import (
	"encoding/json"
	"os"

	"github.com/bitknox/Raven/benchmarking/model"
)

func ParseInput(jsonPath string) (*model.BenchmarkSuite, error) {
	file, err := os.ReadFile(jsonPath)
	if err != nil {
		return nil, err
	}

	var suite model.BenchmarkSuite
	err = json.Unmarshal(file, &suite)
	if err != nil {
		return nil, err
	}

	return &suite, nil
}
