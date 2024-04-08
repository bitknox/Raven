from voronoi import *
from perlin import generate_perlin
import random
import numpy as np
from util import generate_tfw

import click


@click.command()
@click.option("--type", default="perlin", help="Type of image to generate")
@click.option("--selectivity", type=float, default=None, help="Describes how much of the image should be white (0.0 - 1.0). If None, it will instead generate a coloured world")
@click.option("--rescale", default=1, type=int, help="Scales the resulting images by the value")
@click.option("--shape", type=(int, int), default=(1000,1000), help="Original size (before scaling) of the image")
@click.option("--output", default="image.png", help="Output path")
@click.option(
    "--area",
    type=(float, float, float, float),
    default=(90, -180, -90, 180),
    help="Area that the generated polygons cover.",
)
def generate_image(type, selectivity, rescale, shape, output,area):
    if type not in ["perlin", "voronoi"]:
        raise ValueError("Invalid type of image to generate")
    if type != "perlin" and selectivity is not None:
        raise ValueError("Selectivity is only valid for perlin noise")
    
    shape = np.array(shape)
    img = None
    if type == "perlin":
        scale = 250
        octaves = 6
        persistence = 0.5
        lacunarity = 2.0
        seed = np.random.randint(0, 100)
        img = generate_perlin(
            shape, scale, octaves, persistence, lacunarity, seed, selectivity
        )
    elif type == "voronoi":
        img = generate(
            path=output,
            width=shape[0],
            height=shape[1],
            regions=100,
            colors=[generate_random_color() for _ in range(20)],
            color_algorithm=ColorAlgorithm.no_adjacent_same,
        )

    scaled_size = shape * rescale
    img = img.resize((scaled_size[0], scaled_size[1]), Image.NEAREST)
    img.save(output)
    generate_tfw(output.split(".")[0], scaled_size[0], scaled_size[1], area)


if __name__ == "__main__":
    generate_image()
