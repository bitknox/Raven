package environments

import (
	"bufio"
	"fmt"
	"log"
	"os/exec"
	"strings"
	"time"
)

type DockerEnvironment struct {
	DockerFilePath string
	ImageTag       string
	ContainerTag   string
	MountPath      string
}

const MAX_TICKS = 10

// ======================================
// ============ Public Functions ========
// ======================================

func (d *DockerEnvironment) RunCommand(cmd Command) (string, error) {
	//run the command in the container
	args := append([]string{"exec", d.ContainerTag, cmd.Path}, cmd.Args...)
	command := exec.Command("docker", args...)
	stderr, err := command.StderrPipe()

	if err != nil {
		return "", fmt.Errorf("failed to get stdout pipe: %s", err)
	}

	errScanner := bufio.NewScanner(stderr)

	go func() {
		for errScanner.Scan() {
			log.Println(errScanner.Text())
		}
	}()

	out, err := command.Output()

	if err != nil {
		return "", fmt.Errorf("failed to get command output: %s", err)
	}

	return string(out), nil
}

func (d *DockerEnvironment) Setup() error {
	imageTag, err := d.buildImage()
	if err != nil {
		return err
	}
	tag := "benchmark_container_" + time.Now().Format("20060102")

	//run the docker container as a daemon
	out, err := exec.Command("docker", "run", "--rm", "-v", fmt.Sprintf("%s:/home", d.MountPath), "-v", fmt.Sprintf("%s:/root/.ivy2/cache", d.MountPath+"/cache"), "--name", tag, "-d", imageTag).CombinedOutput()

	if err != nil {
		fmt.Println(string(out))
		return fmt.Errorf("failed to run docker container: %s", err)
	}
	ticker := time.NewTicker(500 * time.Millisecond)
	defer ticker.Stop()
	ticks := 0

	//wait for the container to be up and running
	for range ticker.C {
		if ticks > MAX_TICKS {
			return fmt.Errorf("container did not start in time")
		}
		ticks++
		out, err := exec.Command("docker", "inspect", "-f", "{{.State.Running}}", tag).Output()
		if err != nil {
			fmt.Println("failed to inspect container: ", err)
		}
		if strings.TrimSpace(string(out)) == "true" {
			break
		}
	}
	d.ContainerTag = tag
	if err != nil {
		return err
	}
	return nil
}

func (d *DockerEnvironment) Teardown() error {
	//stop the container

	err := exec.Command("docker", "stop", d.ContainerTag).Run()
	if err != nil {
		return err
	}
	return nil
}

// ======================================
// ============ Private Functions ========
// ======================================

func (d *DockerEnvironment) buildImage() (string, error) {
	//create a unique tag for the docker image
	tag := "benchmark_image_" + time.Now().Format("20060102")
	fmt.Println(d.DockerFilePath)
	//build the docker image from the dockerfile, giving it a unique tag that can be used to run the container
	command := exec.Command("docker", "build", d.DockerFilePath, "-t", tag, "-f", d.DockerFilePath+"Dockerfile")

	out, err := command.CombinedOutput()

	if err != nil {
		fmt.Println(string(out))
		return "", fmt.Errorf("failed to build docker image: %s", err)
	}
	return tag, nil
}
