package parsing

import (
	"encoding/json"
	"os"

	"github.com/bitknox/Raven/benchmarking/model"
)

func ParseResult(jsonInput string) (*model.BenchmarkResult, error) {

	var result = model.BenchmarkResult{}
	err := json.Unmarshal([]byte(jsonInput), &result)
	if err != nil {
		return nil, err
	}

	return &result, nil
}

func WriteResults(results []*model.BenchmarkResult, path string) error {
	file, err := json.Marshal(results)
	if err != nil {
		return err
	}
	err = os.WriteFile(path, file, 0644)
	if err != nil {
		return err
	}
	return nil
}

func ResultIsValid(result *model.BenchmarkResult) bool {

	return result.Name != "" && len(result.Times) == result.Iterations && result.Iterations > 0
}
