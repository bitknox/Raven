import noise
import numpy as np
from PIL import Image

#Normalize the world to 0-255
def rgb_norm(world):
    world_min = np.min(world)
    world_max = np.max(world)
    norm = lambda x: (x-world_min/(world_max - world_min))*255
    return np.vectorize(norm)

#Prep the world for saving
def prep_world(world):
    norm = rgb_norm(world)
    world = norm(world)
    return world

#Parameters
def generate_perlin(shape, scale, octaves, persistence, lacunarity, seed, output):
	world = np.zeros(shape)
	for i in range(shape[0]):
			for j in range(shape[1]):
					world[i][j] = noise.pnoise2(i/scale, 
																			j/scale, 
																			octaves=octaves, 
																			persistence=persistence, 
																			lacunarity=lacunarity, 
																			repeatx=shape[0], 
																			repeaty=shape[1], 
																			base=seed)



	img = Image.fromarray(prep_world(world))
	if img.mode != 'L':
			img = img.convert('L')
	return img
    
    