package dk.itu.raven.util;

import java.io.Serializable;
import java.util.Iterator;

/*
 * Copyright (c) 2001-2009, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Implementation of a bit map of any size, together with static methods to
 * manipulate int, byte and byte[] values as bit maps.
 *
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 1.9.0
 * @since 1.8.0
 */
public class BitMap implements Iterator<Integer>, Iterable<Integer>, Serializable {

    int defaultCapacity;
    int capacity;
    int[] map;
    int limitPos;
    int currentIteratorIndex;

    public BitMap(int initialCapacity) {
        if (initialCapacity <= 0)
            throw new IllegalArgumentException("initialCapacity must be positive");

        int words = initialCapacity / 32;

        if (initialCapacity % 32 != 0) {
            words++;
        }

        defaultCapacity = capacity = words * 32;
        map = new int[words];
        limitPos = 0;
    }

    public int[] getMap() {
        return map;
    }

    public int size() {
        return limitPos;
    }

    public void setSize(int size) {
        limitPos = size;
    }

    @Override
    public boolean hasNext() {
        return currentIteratorIndex < limitPos;
    }

    @Override
    public Integer next() {
        return getOrZero(++currentIteratorIndex);
    }

    @Override
    public Iterator<Integer> iterator() {
        currentIteratorIndex = 0;
        return this;
    }

    /**
     * Resets to blank with original capacity
     */
    public void reset() {

        map = new int[defaultCapacity / 32];
        capacity = defaultCapacity;
        limitPos = 0;
    }

    public int setTo(int pos, int val) {
        if (val == 0) {
            return unset(pos);
        } else {
            return set(pos);
        }
    }

    /**
     * Flips pos and returns old value
     */
    public int flip(int pos) {

        while (pos >= capacity) {
            doubleCapacity();
        }

        if (pos >= limitPos) {
            limitPos = pos + 1;
        }

        int windex = pos >> 5;
        int mask = 0x80000000 >>> (pos & 0x1F);
        int word = map[windex];
        int result = (word & mask) == 0 ? 0
                : 1;

        map[windex] = (word ^ mask);

        return result;
    }

    /**
     * Sets pos and returns old value
     */
    public int set(int pos) {

        while (pos >= capacity) {
            doubleCapacity();
        }

        if (pos >= limitPos) {
            limitPos = pos + 1;
        }

        int windex = pos >> 5;
        int mask = 0x80000000 >>> (pos & 0x1F);
        int word = map[windex];
        int result = (word & mask) == 0 ? 0
                : 1;

        map[windex] = (word | mask);

        return result;
    }

    /**
     * Unsets pos and returns old value
     */
    public int unset(int pos) {

        while (pos >= capacity) {
            doubleCapacity();
        }

        if (pos >= limitPos) {
            limitPos = pos + 1;

            return 0;
        }

        int windex = pos >> 5;
        int mask = 0x80000000 >>> (pos & 0x1F);
        int word = map[windex];
        int result = (word & mask) == 0 ? 0
                : 1;

        mask = ~mask;
        map[windex] = (word & mask);

        return result;
    }

    public int getOrZero(int pos) {
        if (pos >= limitPos) {
            return 0;
        }

        int windex = pos >> 5;
        int mask = 0x80000000 >>> (pos & 0x1F);
        int word = map[windex];

        return (word & mask) == 0 ? 0 : 1;
    }

    public boolean isSet2(int pos) {
        final int windex = pos >> 5;
        final int mask = 0x80000000 >>> (pos & 0x1F);
        final int word = map[windex];

        return (word & mask) != 0;
    }

    public boolean isSet(int pos) {
        return getOrZero(pos) == 1;
    }

    public byte[] getBytes() {

        byte[] buf = new byte[(limitPos + 7) / 8];

        if (buf.length == 0) {
            return buf;
        }

        for (int i = 0;;) {
            int v = map[i / 4];

            buf[i++] = (byte) (v >>> 24);

            if (i == buf.length) {
                break;
            }

            buf[i++] = (byte) (v >>> 16);

            if (i == buf.length) {
                break;
            }

            buf[i++] = (byte) (v >>> 8);

            if (i == buf.length) {
                break;
            }

            buf[i++] = (byte) v;

            if (i == buf.length) {
                break;
            }
        }

        return buf;
    }

    public void concat(BitMap other) {
        int windex = this.limitPos >> 5;

        int newLimit = this.limitPos + other.limitPos;
        // make space
        int[] newmap = new int[this.map.length + other.map.length + 2];
        System.arraycopy(this.map, 0, newmap, 0, this.map.length);
        this.map = newmap;
        capacity = 32 * this.map.length;

        int relativePos = this.limitPos % 32; // maybe limitpos -1
        int endMask = (-1) >>> (32 - relativePos);
        endMask = relativePos == 0 ? 0 : endMask;
        int startMask = ~endMask;
        for (int word : other.map) {
            this.map[windex] |= (word & startMask) >>> relativePos;
            windex++;
            this.map[windex] = (word & endMask) << (32 - relativePos);
        }

        this.limitPos = newLimit;
    }

    public void setInt(int idx, int val, int length) {
        int windex = idx >> 5;

        val <<= (32 - length);

        int newLimit = this.limitPos + length;

        capacity = 32 * this.map.length;

        int relativePos = idx % 32;
        int endMask = (-1) >>> (32 - relativePos);
        endMask = relativePos == 0 ? 0 : endMask;
        int startMask = ~endMask;
        this.map[windex] |= (val & startMask) >>> relativePos;
        windex++;
        this.map[windex] = (val & endMask) << (32 - relativePos);

        this.limitPos = newLimit;
    }

    public void setLong(int idx, long val, int length) {
        if (length < 32) {
            setInt(idx, (int) (val), Math.min(length, 32));
        } else {
            val <<= (64 - length);
            setInt(idx, (int) (val >>> 32), Math.min(length, 32));
            if (length > 32) {
                setInt(idx + 32, ((int) val) >>> (64 - length), length - 32);
            }
        }
    }

    public int getInt(int idx, int length) {
        final int windex = idx >> 5;
        final int intBits = (idx & 31);

        final int endMask = ((-1) << (32 - length)) >>> intBits;
        final int remaining = length + intBits - 32;
        int startMask = 0;
        if (remaining > 0) {
            startMask = (-1) << (32 - remaining);
        }
        int res = (this.map[windex] & endMask);
        if ((32 - length) - intBits > 0) {
            res >>>= ((32 - length) - intBits);
        } else {
            res <<= ((length) + intBits);
        }
        res += (this.map[windex + 1] & startMask) >>> (32 - remaining);
        return res;
    }

    public long getLong(int idx, int length) {
        long res = 0xFFFFFFFFL & (getInt(idx, Math.min(length, 32)));
        if (length > 32) {
            res <<= (length - 32);
            res += getInt(idx + 32, length - 32);
        }
        return res;
    }

    private void doubleCapacity() {

        int[] newmap = new int[map.length * 2];

        capacity *= 2;

        System.arraycopy(map, 0, newmap, 0, map.length);

        map = newmap;
    }

    /**
     * copy the byte value into the map at given position (0, 24)
     */
    public static int setByte(int map, byte value, int pos) {

        int intValue = (value & 0xff) << (24 - pos);
        int mask = 0xff000000 >>> pos;

        mask = ~mask;
        map &= mask;

        return (map | intValue);
    }

    public void setBytes(byte[] map, int pos) {

        int mask = 0x00000080 >>> (pos & 0x07);
        int index = pos / 8;

        if (index >= map.length) {
            return;
        }

        byte b = map[index];

        map[index] = (byte) (b | mask);
    }

    public static int set(int map, int pos) {

        int mask = 0x80000000 >>> pos;

        return (map | mask);
    }

    public static byte set(byte map, int pos) {

        int mask = 0x00000080 >>> pos;

        return (byte) (map | mask);
    }

    public static int unset(int map, int pos) {

        int mask = 0x80000000 >>> pos;

        mask = ~mask;

        return (map & mask);
    }

    public static boolean isSet(int map, int pos) {

        int mask = 0x80000000 >>> pos;

        return (map & mask) == 0 ? false
                : true;
    }

    public static boolean isSet(byte map, int pos) {

        int mask = 0x00000080 >>> pos;

        return (map & mask) == 0 ? false
                : true;
    }

    public static boolean isSet(byte[] map, int pos) {

        int mask = 0x00000080 >>> (pos & 0x07);
        ;
        int index = pos / 8;

        if (index >= map.length) {
            return false;
        }

        byte b = map[index];

        return (b & mask) == 0 ? false
                : true;
    }

    public static void unset(byte[] map, int pos) {

        int mask = 0x00000080 >>> (pos & 0x07);

        mask = ~mask;

        int index = pos / 8;

        if (index >= map.length) {
            return;
        }

        byte b = map[index];

        map[index] = (byte) (b & mask);
    }

    public static void set(byte[] map, int pos) {

        int mask = 0x00000080 >>> (pos & 0x07);
        int index = pos / 8;

        if (index >= map.length) {
            return;
        }

        byte b = map[index];

        map[index] = (byte) (b | mask);
    }

    /**
     * AND count bits from source with map contents starting at pos
     */
    public static void and(byte[] map, int pos, byte source, int count) {

        int shift = pos & 0x07;
        int mask = (source & 0xff) >>> shift;
        int innermask = 0xff >> shift;
        int index = pos / 8;

        if (count < 8) {
            innermask = innermask >>> (8 - count);
            innermask = innermask << (8 - count);
        }

        mask &= innermask;
        innermask = ~innermask;

        if (index >= map.length) {
            return;
        }

        byte b = map[index];

        map[index] = (byte) (b & innermask);
        b = (byte) (b & mask);
        map[index] = (byte) (map[index] | b);

        if (shift == 0) {
            return;
        }

        shift = 8 - shift;

        if (count > shift) {
            mask = ((source & 0xff) << 8) >>> shift;
            innermask = 0xff00 >>> shift;
            innermask = ~innermask;
            b = map[index + 1];
            map[index + 1] = (byte) (b & innermask);
            b = (byte) (b & mask);
            map[index + 1] = (byte) (map[index + 1] | b);
        }
    }

    /**
     * OR count bits from source with map contents starting at pos
     */
    public static void or(byte[] map, int pos, byte source, int count) {

        int shift = pos & 0x07;
        int mask = (source & 0xff) >>> shift;
        int index = pos / 8;

        if (index >= map.length) {
            return;
        }

        byte b = (byte) (map[index] | mask);

        map[index] = b;

        if (shift == 0) {
            return;
        }

        shift = 8 - shift;

        if (count > shift) {
            mask = ((source & 0xff) << 8) >>> shift;
            b = (byte) (map[index + 1] | mask);
            map[index + 1] = b;
        }
    }

    /**
     * overlay count bits from source on map contents starting at pos
     */
    public static void overlay(byte[] map, int pos, byte source, int count) {

        int shift = pos & 0x07;
        int mask = (source & 0xff) >>> shift;
        int innermask = 0xff >> shift;
        int index = pos / 8;

        if (count < 8) {
            innermask = innermask >>> (8 - count);
            innermask = innermask << (8 - count);
        }

        mask &= innermask;
        innermask = ~innermask;

        if (index >= map.length) {
            return;
        }

        byte b = map[index];

        b = (byte) (b & innermask);
        map[index] = (byte) (b | mask);

        if (shift == 0) {
            return;
        }

        shift = 8 - shift;

        if (count > shift) {
            mask = ((source & 0xff) << 8) >>> shift;
            innermask = 0xff00 >>> shift;
            innermask = ~innermask;
            b = map[index + 1];
            b = (byte) (b & innermask);
            map[index + 1] = (byte) (b | mask);
        }
    }

    public static int compare(byte[] a, byte[] b) {

        int shortLength = a.length > b.length ? b.length
                : a.length;

        for (int i = 0; i < shortLength; i++) {
            if (a[i] == b[i]) {
                continue;
            }

            return (((int) a[i]) & 0xff) > (((int) b[i]) & 0xff) ? 1
                    : -1;
        }

        if (a.length == b.length) {
            return 0;
        }

        return a.length > b.length ? 1
                : -1;
    }

    public static byte[] and(byte[] a, byte[] b) {

        int length = a.length > b.length ? a.length
                : b.length;
        int shortLength = a.length > b.length ? b.length
                : a.length;
        byte[] map = new byte[length];

        for (int i = 0; i < shortLength; i++) {
            map[i] = (byte) (a[i] & b[i]);
        }

        return map;
    }

    public static byte[] or(byte[] a, byte[] b) {

        int length = a.length > b.length ? a.length
                : b.length;
        int shortLength = a.length > b.length ? b.length
                : a.length;
        byte[] map = new byte[length];

        if (length != shortLength) {
            byte[] source = a.length > b.length ? a
                    : b;

            System.arraycopy(source, shortLength, map, shortLength,
                    length - shortLength);
        }

        for (int i = 0; i < shortLength; i++) {
            map[i] = (byte) (a[i] | b[i]);
        }

        return map;
    }

    public static byte[] xor(byte[] a, byte[] b) {

        int length = a.length > b.length ? a.length
                : b.length;
        int shortLength = a.length > b.length ? b.length
                : a.length;
        byte[] map = new byte[length];

        if (length != shortLength) {
            byte[] source = a.length > b.length ? a
                    : b;

            System.arraycopy(source, shortLength, map, shortLength,
                    length - shortLength);
        }

        for (int i = 0; i < shortLength; i++) {
            map[i] = (byte) (a[i] ^ b[i]);
        }

        return map;
    }

    public static byte[] not(byte[] a) {

        byte[] map = new byte[a.length];

        for (int i = 0; i < a.length; i++) {
            map[i] = (byte) ~a[i];
        }

        return map;
    }

    public static boolean hasAnyBitSet(byte[] map) {

        for (int i = 0; i < map.length; i++) {
            if (map[i] != 0) {
                return true;
            }
        }

        return false;
    }

    public static byte[] leftShift(byte[] map, int shiftBits) {

        byte[] newMap = new byte[map.length];
        int shiftBytes = shiftBits / 8;

        if (shiftBytes >= map.length) {
            return newMap;
        }

        shiftBits = shiftBits % 8;

        if (shiftBits == 0) {
            for (int i = 0, j = shiftBytes; j < map.length; i++, j++) {
                newMap[i] = map[j];
            }
        } else {
            for (int i = 0, j = shiftBytes; j < map.length; i++, j++) {
                int shifted = (map[j] & 0xff) << shiftBits;

                newMap[i] = (byte) shifted;

                if (i > 0) {
                    newMap[i - 1] |= (byte) (shifted >>> 8);
                }
            }
        }

        return newMap;
    }
}
