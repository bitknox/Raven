package dk.itu.raven.ksquared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.Stack;

import org.junit.jupiter.api.Test;
import org.geotools.api.metadata.citation.Address;
import org.junit.jupiter.api.RepeatedTest;

import dk.itu.raven.util.matrix.RandomMatrix;
import dk.itu.raven.join.Square;
import dk.itu.raven.util.LongArrayWrapper;
import dk.itu.raven.util.PrimitiveArrayWrapper;
import dk.itu.raven.util.matrix.ArrayMatrix;
import dk.itu.raven.util.matrix.LongArrayMatrix;
import dk.itu.raven.util.matrix.Matrix;

public class KSquaredLongTest {
	private final long[][] M = {
			{ 5, 5, 4, 4, 4, 4, 1, 1 }, //
			{ 5, 4, 4, 4, 4, 4, 1, 1 }, //
			{ 4, 4, 4, 4, 1, 2, 2, 1 }, //
			{ 3, 3, 4, 3, 2, 1, 2, 2 }, //
			{ 3, 4, 3, 3, 2, 2, 2, 2 }, //
			{ 4, 3, 3, 2, 2, 2, 2, 2 }, //
			{ 1, 1, 1, 3, 2, 2, 2, 2 }, //
			{ 1, 1, 1, 2, 2, 2, 2, 2 } }; //

	private final long[][] MLong = {
			{ 5, 5, 4, 4, 4, 4, 1, 1 }, //
			{ 5, 4, 4, 4, 4, 4, 1, 1 }, //
			{ 4, 4, 4, 4000000000L, 1, 2, 2, 1 }, //
			{ 3, 3, 4, 3, 2, 1, 2, 2 }, //
			{ 3, 4, 3, 3, 2, 2, 2, 2 }, //
			{ 4, 3, 3, 2, 2, 2, 2, 2 }, //
			{ 1, 1, 1, 3, 2, 2, 2, 2 }, //
			{ 1, 1, 1, 2, 2, 2, 2, 2 } }; //

	@Test
	public void K2RasterLongWorksAsNormalK2Raster() {
		Matrix m = new LongArrayMatrix(M, 8, 8);

		AbstractK2Raster k2i = new K2RasterIntBuilder().build(m, 2);
		AbstractK2Raster k2l = new K2RasterBuilder().build(m, 2);

		PrimitiveArrayWrapper expected = k2i.getWindow(0, k2i.getSize()-1, 0, k2i.getSize()-1);
		PrimitiveArrayWrapper actual = k2l.getWindow(0, k2l.getSize()-1, 0, k2l.getSize()-1);
		for (int i = 0; i < expected.length(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}

	@Test
	public void K2RasterLongSupportsLongs() {
		Matrix m = new LongArrayMatrix(MLong, 8, 8);
		AbstractK2Raster k2l = new K2RasterBuilder().build(m, 2);
		assertEquals(4000000000L, k2l.getCell(2, 3));
	}

	@Test
	public void K2RasterLongGetWindowSupportsLongs() {
		final long[][] arr = 	{{4000000000L,4000000001L,4000000002L,4000000003L},
								 {4000000004L,4000000005L,4000000006L,4000000007L},
								 {4000000008L,4000000009L,4000000010L,4000000011L},
								 {4000000012L,4000000013L,4000000014L,4000000015L}};
		Matrix m = new LongArrayMatrix(arr, 4, 4);
		AbstractK2Raster k2l = new K2RasterBuilder().build(m, 2);
		PrimitiveArrayWrapper expected = new LongArrayWrapper(new long[] {4000000000L,4000000001L,4000000004L,4000000005L,4000000002L,4000000003L,4000000006L,4000000007L,4000000008L,4000000009L,4000000012L,4000000013L,4000000010L,4000000011L,4000000014L,4000000015L});
		PrimitiveArrayWrapper actual = k2l.getWindow(0, 3, 0, 3);
		for (int i = 0; i < 16; i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}

}
