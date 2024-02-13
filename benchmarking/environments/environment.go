package environments

type EnvironmentType string

const (
	Local  EnvironmentType = "local"
	Docker EnvironmentType = "docker"
)

type Command struct {
	Path string   `json:"path"`
	Args []string `json:"args"`
}

type CommandEnvironment interface {
	Setup() error
	Teardown() error
	RunCommand(command Command) (string, error)
}

type EnvironmentOptions struct {
	EnvironmentType EnvironmentType `json:"environment_type"`
	DockerFilePath  string          `json:"docker_file_path"`
	DockerMountPath string          `json:"docker_mount_path"`
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
