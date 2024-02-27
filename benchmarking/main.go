package main

import (
	"fmt"
	"log"
	"os"

	"github.com/bitknox/Raven/benchmarking/executor"
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
				Value:   "./test/results.json",
			},
		},
		Name:  "raven-benchmarking",
		Usage: "benchmarking tool for raven project",
		Action: func(c *cli.Context) error {
			plotter.VerifyEnvironment()
			benchmarks, err := parsing.ParseInput(c.String("input"))

			if err != nil {
				fmt.Println(err)
				return err
			}
			//use executor to run the benchmars
			results := executor.ExecuteBenchmarks(benchmarks)

			resultOutputDirectory := c.String("output")

			//write results to file
			err = parsing.WriteResults(results, resultOutputDirectory)

			if err != nil {
				fmt.Println(err)
				return err
			}

			//plot results
			plotter.PlotResults(resultOutputDirectory, "./test/output.png")

			return nil
		},
	}

	if err := app.Run(os.Args); err != nil {
		log.Fatal(err)
	}
}
