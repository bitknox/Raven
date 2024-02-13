package environments

type EnvironmentType int64

const (
	Local EnvironmentType = iota
	Docker
)

type Command struct {
	Path string
	Args []string
}

type CommandEnvironment interface {
	Setup() error
	Teardown() error
	RunCommand(command Command) (string, error)
}

type EnvironmentOptions struct {
	EnvironmentType EnvironmentType
	DockerFilePath  string
}

type Environment struct {
	Type   EnvironmentType
	Runner CommandEnvironment
}

func NewEnvironment(options EnvironmentOptions) *Environment {
	var runner CommandEnvironment
	switch options.EnvironmentType {
	case Local:
		runner = &LocalEnvironment{}
	case Docker:
		runner = &DockerEnvironment{
			DockerFilePath: options.DockerFilePath,
		}
	}
	return &Environment{
		Type:   options.EnvironmentType,
		Runner: runner,
	}
}
