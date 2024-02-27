package parsing

import (
	"encoding/json"

	"github.com/bitknox/Raven/benchmarking/model"
)

func ParseResult(jsonInput string) (*model.BenchmarkResult, error) {

	var result *model.BenchmarkResult
	err := json.Unmarshal([]byte(jsonInput), &result)
	if err != nil {
		return nil, err
	}
	return result, nil
}

func ResultIsValid(result *model.BenchmarkResult) bool {

	return result.Name != "" && len(result.Times) == result.Iterations && result.Iterations > 0
}
