package environments

import (
	"fmt"
	"os/exec"
	"time"
)

type DockerEnvironment struct {
	DockerFilePath string
	ImageTag       string
}

// ======================================
// ============ Public Functions ========
// ======================================

func (d *DockerEnvironment) RunCommand(cmd Command) (string, error) {
	return "", nil
}

func (d *DockerEnvironment) Setup() error {
	imageTag, err := d.buildImage()
	if err != nil {
		return err
	}
	tag := "benchmark_container_" + time.Now().Format("20060102150405")

	//run the docker container as a daemon
	err = exec.Command("docker", "run", "--name", tag, "-d", imageTag).Run()

	if err != nil {
		return err
	}
	return nil
}

func (d *DockerEnvironment) Teardown() error {
	return nil
}

// ======================================
// ============ Privat Functions ========
// ======================================

func (d *DockerEnvironment) buildImage() (string, error) {
	//create a unique tag for the docker image
	tag := "benchmar_image_" + time.Now().Format("20060102150405")
	fmt.Println(d.DockerFilePath)
	//build the docker image from the dockerfile, giving it a unique tag that can be used to run the container
	err := exec.Command("docker", "build", ".", "-t", tag, "-f", d.DockerFilePath).Run()

	if err != nil {
		return "", err
	}
	return tag, nil
}
