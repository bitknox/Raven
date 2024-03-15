package dk.itu.raven.io.cache;

import java.io.Serializable;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.ksquared.AbstractK2Raster;

public class CachedRasterStructure implements Serializable {
	public CachedRasterStructure(AbstractK2Raster raster, Offset<Integer> offset, Offset<Integer> globalOffset) {
		this.raster = raster;
		this.offset = offset;
		this.globalOffset = globalOffset;
	}

	public AbstractK2Raster raster;
	public Offset<Integer> offset;
	public Offset<Integer> globalOffset;
}