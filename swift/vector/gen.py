import shapefile
from io import BytesIO as StringIO
from gen_shape import generate_polygon, clip
import shutil
import math
import random
import click
import os

@click.command()
@click.option("--polygons", default=10000, help="Number of polygons to generate")
@click.option(
    "--density", type=float, default=100, help="Fake density of the polygons (percent) :)"
)
@click.option(
    "--area",
    type=(float, float, float, float),
    default=(90, -180, -90, 180),
    help="Area that the generated polygons cover.",
)
@click.option("--output", default="./out/geom.shp", help="Output path")
@click.option("--num-vertices", default=20, help="Average number of vertices per polygon")
def generate_vector(polygons, density, area, output, num_vertices):
    shp = StringIO()
    shx = StringIO()
    dbf = StringIO()

    dy = (area[0] - area[2]) 
    dx = (area[3] - area[1]) 

    cy = math.sqrt(polygons * dy / dx)
    cx = polygons / cy
    cy += 1
    cx += 1

    jumpx = dx / cx
    jumpy = dy / cy

    sizex = dx * math.sqrt(density / 100) / cx
    sizey = dy * math.sqrt(density / 100) / cy
    size = min(sizey, sizex) / 2.2

    w = shapefile.Writer(output, shp=shp, shx=shx, dbf=dbf)
    w.field("name", "C")
    x = area[1] + jumpx / 2
    y = area[2] + jumpy / 2
    for i in range(polygons):
        w.poly(
            [
                generate_polygon(
                    center=(x, y),
                    avg_radius=size,
                    irregularity=1,
                    spikiness=0.3,
                    num_vertices=int(clip(random.gauss(num_vertices, 0.5), 0, 2 * num_vertices)),
                ),  # poly 1
            ]
        )
        x += jumpx
        if x > area[3] - jumpx / 2:
            x = area[1] + jumpx / 2
            y += jumpy
        w.record("polygon"+str(i))
    w.close()
    shutil.make_archive('out', format='zip', root_dir=os.path.dirname(output))
    


if __name__ == "__main__":
    generate_vector()