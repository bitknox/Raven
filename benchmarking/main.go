package main

import (
	"fmt"
	"log"
	"os"

	"github.com/bitknox/Raven/benchmarking/executor"
	"github.com/bitknox/Raven/benchmarking/parsing"
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
		},
		Name:  "raven-benchmarking",
		Usage: "benchmarking tool for raven project",
		Action: func(c *cli.Context) error {

			model, err := parsing.ParseInput(c.String("input"))

			if err != nil {
				fmt.Println(err)
				return err
			}
			//use executor to run the benchmars
			results := executor.ExecuteBenchmarks(model)

			//print results
			for _, result := range results {
				fmt.Printf("%+v\n", result)
			}

			return nil
		},
	}

	if err := app.Run(os.Args); err != nil {
		log.Fatal(err)
	}
}
