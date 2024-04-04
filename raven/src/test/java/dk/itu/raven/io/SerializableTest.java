
package dk.itu.raven.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import dk.itu.raven.geometry.Offset;
import dk.itu.raven.io.cache.CachedRasterStructure;
import dk.itu.raven.io.serialization.Serializer;
import dk.itu.raven.ksquared.AbstractK2Raster;
import dk.itu.raven.ksquared.K2RasterBuilder;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RandomMatrix;

public class SerializableTest {

	@Test
	public void testSerializableChunk() throws IOException, ClassNotFoundException {
		Offset<Integer> offset = new Offset<Integer>(5, 10);
		Matrix matrix = new RandomMatrix(1000, 1000, 100);
		AbstractK2Raster k2 = new K2RasterBuilder().build(matrix, 2);

		CachedRasterStructure crs = new CachedRasterStructure(k2, offset, "");

		Serializer.serialize("serializeTest.txt", crs);

		CachedRasterStructure crs2 = (CachedRasterStructure) Serializer.deserialize("serializeTest.txt");

		assertEquals(k2.k, crs2.raster.k);
		assertEquals(k2.getSize(), crs2.raster.getSize());
		assertEquals(offset, crs2.offset);
	}

	@AfterAll
	public static void tearDown() {
		File file = new File("serializeTest.txt");
		file.delete();
	}

}