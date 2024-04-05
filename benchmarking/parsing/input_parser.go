package parsing

import (
	"encoding/json"
	"os"

	"github.com/bitknox/Raven/benchmarking/model"
)

func ParseInput(jsonPath string) (*model.InputSuite, error) {
	file, err := os.ReadFile(jsonPath)
	if err != nil {
		return nil, err
	}

	var suite model.InputSuite
	err = json.Unmarshal(file, &suite)
	if err != nil {
		return nil, err
	}

	return &suite, nil
}
