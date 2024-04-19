# SWIFT (Synthetic World Imaging and Feature Toolkit)

A component for generating synthetic vector and raster data

## Prerequisite

- python 3

## Usage (Raster)

```bash
python raster/gen.py
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

### Types of noise
- perlin noise
- voronoi noise

### NOTE
The area uses the EPSG-4326 CRS (lat/long with latitudes ranging from -90 to 90 and longtitudes ranging from -180 to 180).

## Usage (Vector)

```bash
python vector/gen.py
```

```bash
Usage: gen.py [OPTIONS]

Options:
  --polygons INTEGER              Number of polygons to generate
  --density FLOAT                 Fake density of the polygons (percent) :)
  --area <FLOAT FLOAT FLOAT FLOAT>...
                                  Area that the generated polygons cover.
  --output TEXT                   Output path
  --num-vertices INTEGER          Average number of vertices per polygon
  --help                          Show this message and exit.
```

### NOTE
The area uses the EPSG-4326 CRS (lat/long with latitudes ranging from -90 to 90 and longtitudes ranging from -180 to 180).

A density of 100 will only result in the vector data covering around 78%, this is because the shapes resemble circles and the program is made to place them with touching perimeters when the density is 100%. a density of 50% will result in an actual density of $50\% * 78\%=39\%$.

