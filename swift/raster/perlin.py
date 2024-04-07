import noise
import numpy as np
from PIL import Image


# Normalize the world to 0-255
def rgb_norm(world):
    world_min = np.min(world)
    world_max = np.max(world)
    norm = lambda x: int(((x - world_min) / (world_max - world_min)) * 256)
    return np.vectorize(norm)


colour_cutoffs = [30, 60, 110, 130, 190, 230, 256]
colours = [
    0,
    0,
    50,
    0,
    0,
    100,
    0,
    0,
    255,
    255,
    255,
    0,
    0,
    150,
    0,
    100,
    100,
    100,
    255,
    255,
    255,
]


def colour_pixel(x):
    for i in range(len(colour_cutoffs)):
        if x <= colour_cutoffs[i]:
            return i


# Prep the world for saving
def prep_world(world):
    norm = rgb_norm(world)
    world = norm(world)
    world = np.vectorize(colour_pixel)(world)
    return world


# Parameters
def generate_perlin(shape, scale, octaves, persistence, lacunarity, seed, output):
    world = np.zeros(shape)
    for i in range(shape[0]):
        for j in range(shape[1]):
            world[i][j] = noise.pnoise2(
                i / scale,
                j / scale,
                octaves=octaves,
                persistence=persistence,
                lacunarity=lacunarity,
                repeatx=shape[0],
                repeaty=shape[1],
                base=seed,
            )

    img = Image.fromarray(prep_world(world))
    if img.mode != "P":
        img = img.convert("P")
    img.putpalette(data=colours)
    return img
