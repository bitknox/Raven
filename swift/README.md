# SWIFT (Synthetic World Imaging and Feature Toolkit)

A component for generating synthetic vector and raster data

## Prerequisite

- python 3

## Usage

```bash
python gen.py
```

```bash
Usage: gen.py [OPTIONS]

Options:
  --type TEXT                     Type of image to generate
  --selectivity FLOAT             Describes how much of the image should be
                                  white (0.0 - 1.0). If None, it will instead
                                  generate a coloured world
  --rescale INTEGER               Scales the resulting images by the value
  --shape <INTEGER INTEGER>...    Original size (before scaling) of the image
  --output TEXT                   Output path
  --area <FLOAT FLOAT FLOAT FLOAT>...
                                  Area that the generated polygons cover.
  --help                          Show this message and exit.
```

### 