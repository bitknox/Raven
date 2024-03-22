package dk.itu.raven.ksquared;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Stack;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import dk.itu.raven.geometry.PixelRange;
import dk.itu.raven.io.serialization.Serializer;
import dk.itu.raven.join.JoinFilterFunctions;
import dk.itu.raven.join.Square;
import dk.itu.raven.util.PrimitiveArrayWrapper;
import dk.itu.raven.util.matrix.ArrayMatrix;
import dk.itu.raven.util.matrix.Matrix;
import dk.itu.raven.util.matrix.RandomMatrix;

public class KSquaredTest {
	private final int[][] M = {
			{ 5, 5, 4, 4, 4, 4, 1, 1 }, //
			{ 5, 4, 4, 4, 4, 4, 1, 1 }, //
			{ 4, 4, 4, 4, 1, 2, 2, 1 }, //
			{ 3, 3, 4, 3, 2, 1, 2, 2 }, //
			{ 3, 4, 3, 3, 2, 2, 2, 2 }, //
			{ 4, 3, 3, 2, 2, 2, 2, 2 }, //
			{ 1, 1, 1, 3, 2, 2, 2, 2 }, //
			{ 1, 1, 1, 2, 2, 2, 2, 2 } }; //

	@Test
	public void testGetCell() {
		Matrix matrix = new RandomMatrix(1000, 1000, 1000000);
		AbstractK2Raster k2Raster = new K2RasterBuilder().build(matrix, 2);
		for (int i = 0; i < matrix.getHeight(); i++) {
			for (int j = 0; j < matrix.getWidth(); j++) {
				assertEquals(matrix.get(i, j), k2Raster.getCell(i, j));
			}
		}
	}

	@RepeatedTest(10)
	public void testGetWindowRow() {
		Random r = new Random();
		Matrix matrix = new RandomMatrix(100, 100, 1000000);
		AbstractK2Raster k2Raster = new K2RasterBuilder().build(matrix, 2);
		int row = r.nextInt(100);
		int col1 = r.nextInt(25);
		int col2 = 75 + r.nextInt(25);
		PrimitiveArrayWrapper res = k2Raster.getWindow(row, row, col1, col2);
		for (int i = 0; i < res.length(); i++) {
			assertEquals(res.get(i), matrix.get(row, col1 + i));
		}
	}

	private void testElements(int[] a1, int[] a2) {
		assertEquals(a1.length, a2.length);
		for (int i = 0; i < a1.length; i++) {
			assertEquals(a1[i], a2[i]);
		}
	}

	@Test
	public void testWithNonSquareMatrix() {
		Matrix matrix = new RandomMatrix(2000, 500, 1000000);
		AbstractK2Raster k2Raster = new K2RasterBuilder().build(matrix, 2);
		for (int i = 0; i < matrix.getHeight(); i++) {
			PrimitiveArrayWrapper row = k2Raster.getWindow(i, i, 0, matrix.getWidth() - 1);
			for (int j = 0; j < matrix.getWidth(); j++) {
				assertEquals(matrix.get(i, j), row.get(j));
			}
		}
	}

	@Test
	public void testGetChildren() {
		AbstractK2Raster k2 = new K2RasterBuilder().build(new ArrayMatrix(M, 8, 8), 2);
		testElements(k2.getChildren(0), new int[] { 1, 2, 3, 4 });
		testElements(k2.getChildren(1), new int[] { 5, 6, 7, 8 });
		testElements(k2.getChildren(2), new int[] { 9, 10, 11, 12 });
		testElements(k2.getChildren(3), new int[] { 13, 14, 15, 16 });
		testElements(k2.getChildren(4), new int[] {});
		testElements(k2.getChildren(5), new int[] { 17, 18, 19, 20 });
	}

	@Test
	public void testVmin() {
		AbstractK2Raster k2 = new K2RasterBuilder().build(new ArrayMatrix(M, 8, 8), 2);
		assertEquals(3, k2.computeVMin(5, 1, 1));
		assertEquals(1, k2.computeVMin(5, 1, 2));
		assertEquals(1, k2.computeVMin(5, 1, 3));
		assertEquals(2, k2.computeVMin(5, 1, 4));
		assertEquals(4, k2.computeVMin(5, 3, 5));
		assertEquals(1, k2.computeVMin(4, 1, 10));
		assertEquals(1, k2.computeVMin(4, 1, 14));
	}

	@Test
	public void testVmax() {
		AbstractK2Raster k2 = new K2RasterBuilder().build(new ArrayMatrix(M, 8, 8), 2);
		assertEquals(5, k2.computeVMax(5, 1));
		assertEquals(4, k2.computeVMax(5, 2));
		assertEquals(4, k2.computeVMax(5, 3));
		assertEquals(2, k2.computeVMax(5, 4));
		assertEquals(5, k2.computeVMax(5, 5));
		assertEquals(1, k2.computeVMax(4, 10));
		assertEquals(2, k2.computeVMax(4, 14));
	}

	@RepeatedTest(100)
	public void testHasChildren() {
		Matrix matrix = new RandomMatrix(200, 200, 1);
		AbstractK2Raster k2 = new K2RasterBuilder().build(matrix, 2);
		Stack<Square> squares = new Stack<>();
		Stack<Integer> indices = new Stack<>();
		indices.push(0);
		squares.push(new Square(0, 0, k2.getSize()));
		while (!indices.empty()) {
			int index = indices.pop();
			Square square = squares.pop();

			int seen = matrix.get(square.getTopY(), square.getTopX());
			boolean isLeaf = true;

			for (int i = 0; i < square.getSize() && isLeaf; i++) {
				for (int j = 0; j < square.getSize(); j++) {
					if (seen != matrix.get(square.getTopY() + i, square.getTopX() + j)) {
						isLeaf = false;
						break;
					}
				}
			}

			assertEquals(!isLeaf, k2.hasChildren(index));

			int[] children = k2.getChildren(index);
			for (int i = 0; i < children.length; i++) {
				int child = children[i];
				indices.push(child);
				squares.push(square.getChildSquare(square.getSize() / k2.k, i, k2.k));
			}
		}
	}

	@Test
	public void testVMinVMax() {
		Matrix matrix = new RandomMatrix(42, 2000, 2000, 100);
		AbstractK2Raster k2 = new K2RasterBuilder().build(matrix, 2);
		Stack<Square> squares = new Stack<>();
		Stack<Integer> indices = new Stack<>();
		Stack<Integer> parentMin = new Stack<>();
		Stack<Integer> parentMax = new Stack<>();
		indices.push(0);
		squares.push(new Square(0, 0, k2.getSize()));
		parentMin.push(0); // value doesn't matter
		parentMax.push(0); // value doesn't matter
		while (!indices.empty()) {
			int index = indices.pop();
			Square square = squares.pop();
			int parmin = parentMin.pop();
			int parmax = parentMax.pop();

			int min = Integer.MAX_VALUE;
			int max = 0;

			for (int i = 0; i < square.getSize(); i++) {
				for (int j = 0; j < square.getSize(); j++) {
					int val = matrix.get(square.getTopY() + i, square.getTopX() + j);
					min = Math.min(min, val);
					max = Math.max(max, val);
				}
			}

			assertEquals(min, k2.computeVMin(parmax, parmin, index));
			assertEquals(max, k2.computeVMax(parmax, index));

			int[] children = k2.getChildren(index);
			for (int i = 0; i < children.length; i++) {
				int child = children[i];
				indices.push(child);
				squares.push(square.getChildSquare(square.getSize() / k2.k, i, k2.k));
				parentMin.push(min);
				parentMax.push(max);
			}
		}
	}

	@Test
	public void whenSerializingAndDeserializing_ThenObjectIsTheSame()
			throws IOException, ClassNotFoundException {
		Matrix matrix = new RandomMatrix(1000, 1000, 100);
		AbstractK2Raster k2 = new K2RasterBuilder().build(matrix, 2);

		Serializer.serialize("serializeTest.txt", k2);

		AbstractK2Raster k2_after = (K2Raster) Serializer.deserialize("serializeTest.txt");

		assertEquals(k2.k, k2_after.k);
		assertEquals(k2.getSize(), k2_after.getSize());

		PrimitiveArrayWrapper expected = k2.getWindow(0, k2.getSize() - 1, 0, k2.getSize() - 1);
		PrimitiveArrayWrapper actual = k2_after.getWindow(0, k2.getSize() - 1, 0, k2.getSize() - 1);

		for (int i = 0; i < expected.length(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}

	@Test
	public void searchValuesInWindowTest() {
		int width = 2000;
		int height = 2000;
		int filterLow = 3;
		int filterHigh = 7;
		int buffer = 0;
		Matrix mat = new RandomMatrix(42, width, height, 10);
		AbstractK2Raster k2Raster = new K2RasterBuilder().build(mat, 2);
		boolean[][] expected = new boolean[width][height];
		for (int i = buffer; i < height - buffer; i++) {
			for (int j = buffer; j < width - buffer; j++) {
				expected[j][i] = mat.get(i, j) >= filterLow && mat.get(i, j) <= filterHigh;
			}
		}
		boolean[][] actual = new boolean[width][height];
		PixelRange[] filteredValues = k2Raster.searchValuesInWindow(buffer, height - 1 - buffer, buffer,
				width - 1 - buffer,
				JoinFilterFunctions.rangeFilter(filterLow, filterHigh));
		for (PixelRange range : filteredValues) {
			for (int x = range.x1; x <= range.x2; x++) {
				actual[x][range.row] = true;
			}
		}

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				assertEquals(expected[i][j], actual[i][j], "seed: " + 2 + ", index: " + i + ", " + j);
			}
		}
	}

	@Test
	public void testDifferentKValuesGivesSameResult() {
		int width = 1000;
		int height = 1000;
		Matrix mat = new RandomMatrix(42, width, height, 100);
		AbstractK2Raster k2_2 = new K2RasterBuilder().build(mat, 2);
		AbstractK2Raster k2_3 = new K2RasterBuilder().build(mat, 3);
		AbstractK2Raster k2_4 = new K2RasterBuilder().build(mat, 4);
		AbstractK2Raster k2_5 = new K2RasterBuilder().build(mat, 5);

		for (int i = 0; i < height; i++) {
			PrimitiveArrayWrapper expected = k2_2.getWindow(i, i, 0, width - 1);
			PrimitiveArrayWrapper actual3 = k2_3.getWindow(i, i, 0, width - 1);
			PrimitiveArrayWrapper actual4 = k2_4.getWindow(i, i, 0, width - 1);
			PrimitiveArrayWrapper actual5 = k2_5.getWindow(i, i, 0, width - 1);
			assertEquals(expected.length(), actual3.length());
			assertEquals(expected.length(), actual4.length());
			assertEquals(expected.length(), actual5.length());
			for (int j = 0; j < width; j++) {
				assertEquals(expected.get(j), actual3.get(j));
				assertEquals(expected.get(j), actual4.get(j));
				assertEquals(expected.get(j), actual5.get(j));
			}
		}

	}

	@AfterAll
	public static void tearDown() {
		File file = new File("serializeTest.txt");
		file.delete();
	}
}
