package dk.itu.raven.io.cache;

import java.io.Serializable;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.ksquared.AbstractK2Raster;

public class CachedRasterStructure implements Serializable {
	public CachedRasterStructure(AbstractK2Raster raster, Offset<Integer> offset, String imageName) {
		this.raster = raster;
		this.offset = offset;
		this.imageName = imageName;
	}

	public AbstractK2Raster raster;
	public Offset<Integer> offset;
	public String imageName;
}