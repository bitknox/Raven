import noise
import numpy as np
from PIL import Image
import math
import scipy.stats as st


# Normalize the world to 0-255
def rgb_norm(world):
    world_min = np.min(world)
    world_max = np.max(world)
    norm = lambda x: int(((x - world_min) / (world_max - world_min)) * 256)
    return np.vectorize(norm)


colour_cutoffs = [30, 60, 110, 130, 190, 230, 256]
colours = [
    [0,0,50],
    [0,0,100],
    [0,0,255],
    [255,255,0],
    [0,150,0],
    [100,100,100],
    [255,255,255]
]
colours = sum(colours,[])


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
def generate_perlin_inner(shape, scale, octaves, persistence, lacunarity, seed):
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
    return world



def generate_selectivity_cutoff_function(selectivity, sigma, mean):
    z = st.norm.ppf(selectivity)
    a = mean + z*sigma
    print(a)
    def func(x):
        if x < a:
            return 255
        else:
            return 0
    return np.vectorize(func)



def generate_perlin(shape, scale, octaves, persistence, lacunarity, seed, selectivity):
    world = generate_perlin_inner(shape, scale, octaves, persistence, lacunarity, seed)
    if selectivity is None:
        img = Image.fromarray(prep_world(world))
        if img.mode != "P":
            img = img.convert("P")
        img.putpalette(data=colours)
    else:
        sigma = np.std(world)
        mean = np.mean(world)
        cutoff = generate_selectivity_cutoff_function(selectivity,sigma,mean)
        img = Image.fromarray(cutoff(world))
        if img.mode != "L":
            img = img.convert("L")
    
    return img




    