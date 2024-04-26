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

func PlotResults(resultsPath string, outputPath string, timestamp string, subplots bool, splitGroups bool, YLimit int) {

	var args = []string{"./plotter/plotter.py", "-i", resultsPath, "-o", outputPath, "-id", timestamp}
	if subplots {
		args = append(args, "-sp")
	}
	if splitGroups {
		args = append(args, "-sg")
	}
	if YLimit != 0 {
		args = append(args, "-ylim", fmt.Sprintf("%d", YLimit))
	}
	cmd := exec.Command("python", args...)
	err := cmd.Run()
	if err != nil {
		fmt.Println(err)
		log.Fatal("Failed to plot results")
	}
}
