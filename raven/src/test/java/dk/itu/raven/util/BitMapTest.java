package dk.itu.raven.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

public class BitMapTest {
	@Test
	public void testSetTo() {
		BitMap bm = new BitMap(2);
		bm.setTo(0, 0);
		assertFalse(bm.isSet(0));
		bm.setTo(0, 1);
		assertFalse(bm.isSet(1));
		assertTrue(bm.isSet(0));
		bm.setTo(1, 1);
		assertTrue(bm.isSet(1));
	}

	@Test
	public void testFlip() {
		BitMap bm = new BitMap(2);
		bm.set(1);
		assertFalse(bm.isSet(0));
		assertTrue(bm.isSet(1));
		bm.flip(0);
		assertTrue(bm.isSet(0));
		assertTrue(bm.isSet(1));
		bm.flip(0);
		assertFalse(bm.isSet(0));
		assertTrue(bm.isSet(1));
	}

	@Test
	public void testConcat() {
		Random r = new Random(42);
		for (int i = 0; i < 200; i++) {
			int size1 = r.nextInt(1000);
			int size2 = r.nextInt(1000);
			BitMap bm = new BitMap(size1);
			BitMap bm2 = new BitMap(size2);

			BitMap expected = new BitMap(size1 + size2);
			for (int j = 0; j < size1; j++) {
				if (r.nextInt(2) == 1) {
					bm.set(j);
					expected.set(j);
				} else {
					bm.unset(j);
					expected.unset(j);
				}
			}
			for (int j = 0; j < size2; j++) {
				if (r.nextInt(2) == 1) {
					bm2.set(j);
					expected.set(size1 + j);
				} else {
					bm2.unset(j);
					expected.unset(size1 + j);
				}
			}
			bm.concat(bm2);
			for (int j = 0; j < size1 + size2; j++) {
				assertEquals(expected.isSet(j), bm.isSet(j), "i: " + i + " j: " + j);
			}
		}
	}
}
