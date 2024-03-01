package environments

import "os/exec"

type LocalEnvironment struct{}

func (l *LocalEnvironment) RunCommand(command Command) (string, error) {

	out, err := exec.Command(command.Path, command.Args...).Output()

	if err != nil {
		return "", err
	}
	return string(out), nil
}

func (l *LocalEnvironment) Setup() error {
	return nil
}

func (l *LocalEnvironment) Teardown() error {
	return nil
}
