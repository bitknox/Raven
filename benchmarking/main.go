package main

import (
	"fmt"
	"log"
	"os"
	"time"

	"github.com/bitknox/Raven/benchmarking/executor"
	"github.com/bitknox/Raven/benchmarking/model"
	"github.com/bitknox/Raven/benchmarking/parsing"
	"github.com/bitknox/Raven/benchmarking/plotter"
	"github.com/urfave/cli/v2"
)

func main() {
	app := &cli.App{
		Flags: []cli.Flag{
			&cli.StringFlag{
				Name:    "input",
				Aliases: []string{"i"},
				Usage:   "benchmark definition file",
				Value:   "./test/input.json",
			},
			&cli.StringFlag{
				Name:    "output",
				Aliases: []string{"o"},
				Usage:   "benchmark destination",
				Value:   "./test/results",
			},
		},
		Name:  "raven-benchmarking",
		Usage: "benchmarking tool for raven project",
		Action: func(c *cli.Context) error {
			plotter.VerifyEnvironment()
			suite, err := parsing.ParseInput(c.String("input"))

			if err != nil {
				fmt.Println(err)
				return err
			}
			//use executor to run the benchmars
			results := executor.ExecuteBenchmarks(suite.Benchmarks)

			resultOutputDirectory := c.String("output")

			//create output directory if it does not exist
			if _, err := os.Stat(resultOutputDirectory); os.IsNotExist(err) {
				os.MkdirAll(resultOutputDirectory, 0755)
			}

			timestamp := time.Now().Format("2006_01_02_15-04-05")

			resultsPath := fmt.Sprintf("%s/results %s.json", resultOutputDirectory, timestamp)

			var experiment = model.BenchmarkSuiteResult{}
			experiment.Results = results
			experiment.Name = suite.Name

			//write results to file
			err = parsing.WriteResults(&experiment, resultsPath)

			if err != nil {
				fmt.Println(err)
				return err
			}

			//plot results
			plotter.PlotResults(resultsPath, resultOutputDirectory, timestamp)

			return nil
		},
	}

	if err := app.Run(os.Args); err != nil {
		log.Fatal(err)
	}
}
