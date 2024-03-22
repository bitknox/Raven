from voronoi import *;
from perlin import generate_perlin;
import random
import numpy as np
from util import generate_tfw

import click

#Generate voronoi diagram image
def generate_random_color():
		return (random.randint(0, 255), random.randint(0, 255), random.randint(0, 255))


@click.command()
@click.option('--type', default='perlin', help='Type of image to generate')
@click.option('--output', default='image.png', help='Output path')
def generate_image(type, output):
    """Simple program that greets NAME for a total of COUNT times."""
    shape = np.array([1000,1000])
    img = None
    if(type == "perlin"):
        scale = 100
        octaves = 6
        persistence = 0.5
        lacunarity = 2.0
        seed = np.random.randint(0,100)
        img = generate_perlin(shape, scale, octaves, persistence, lacunarity, seed, output)
    elif(type == "voronoi"):
        img = generate(
        path = output,
        width = shape[0],
        height = shape[1],
        regions = 100,
        colors = [generate_random_color() for _ in range(20)],
        color_algorithm = ColorAlgorithm.no_adjacent_same
        )

    scaled_size = shape*10
    img = img.resize((scaled_size[0], scaled_size[1]), Image.NEAREST)
    img.save(output)
    generate_tfw(output.split(".")[0],scaled_size[0], scaled_size[1])


if __name__ == '__main__':
    generate_image()



