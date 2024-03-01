package plotter

import (
	"fmt"
	"log"
	"os/exec"
)

func VerifyEnvironment() {
	// Verify that python is installed
	_, err := exec.LookPath("python")
	if err != nil {
		log.Fatal("Python is not installed. Please install Python.")
	}

	// Verify that pip is installed
	_, err = exec.LookPath("pip")
	if err != nil {
		log.Fatal("pip is not installed. Please install pip.")
	}

	// Verify that matplotlib is installed or install it with pip
	cmd := exec.Command("python", "-c", "import matplotlib")
	err = cmd.Run()
	if err != nil {
		// If matplotlib is not installed, try to install it
		cmd = exec.Command("pip", "install", "matplotlib")
		err = cmd.Run()
		if err != nil {
			log.Fatal("Failed to install matplotlib. Please install it manually.")
		}
	}

	log.Println("Environment verified successfully")

}

func PlotResults(resultsPath string, outputPath string, timestamp string) {
	cmd := exec.Command("python", "./plotter/plotter.py", resultsPath, outputPath, timestamp)
	err := cmd.Run()
	if err != nil {
		fmt.Println(err)
		log.Fatal("Failed to plot results")
	}
}
