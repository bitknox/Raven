package dk.itu.raven.ksquared;

import static dk.itu.raven.ksquared.Basics.*;

/**
 * DAC
 * https://github.com/sladra/DACs/blob/master/src/dacs.c
 */
public class DAC_old {
    public int listLength;
    byte nLevels;
    int tamCode;
    int[] levels;
    int[] levelsIndex;
    int[] iniLevel;
    int[] rankLevels;
    BitRank bS;
    int[] base;
    short[] baseBits;
    int[] tableBase;
    int tamtableBase;
    private static final int FACT_RANK = 20;

    static final int epsilon = 1;

    public DAC_old(int[] list) {

        int listLength = list.length;
        int[] levelSizeAux;
        int[] cont;
        int[] contB;

        short[] kvalues;

        this.listLength = listLength;
        if (listLength == 0)
            throw new IllegalArgumentException("List must not be empty");
        int i;
        int j, k;
        int value, newvalue;
        int bits_BS_len = 0;

        kvalues = optimize2(list);
        int nkvalues = kvalues.length;

        short kval;
        int oldval = 0;
        int newval = 0;

        i = 0;
        int multval = 1;
        do {
            oldval = newval;
            if (i >= nkvalues) {
                kval = bitShiftLeftC(kvalues[nkvalues - 1]);
            } else {
                kval = bitShiftLeftC(kvalues[i]);
            }

            multval *= kval;
            newval = oldval + multval;

            i++;
        } while (oldval < newval);

        this.tamtableBase = i;
        this.tableBase = new int[this.tamtableBase];
        levelSizeAux = new int[this.tamtableBase];
        cont = new int[this.tamtableBase];
        contB = new int[this.tamtableBase];

        oldval = 0;
        newval = 0;
        multval = 1;
        for (i = 0; i < this.tamtableBase; i++) {
            oldval = newval;
            if (i >= nkvalues) {
                kval = bitShiftLeftC(kvalues[nkvalues - 1]);
            } else {
                kval = bitShiftLeftC(kvalues[i]);
            }
            multval *= kval;
            newval = oldval + multval;
            this.tableBase[i] = oldval;
        }

        for (i = 0; i < this.tamtableBase; i++) {
            levelSizeAux[i] = 0;
        }

        for (i = 0; i < listLength; i++) {
            value = list[i];
            for (j = 0; j < this.tamtableBase; j++)
                if (value >= this.tableBase[j])
                    levelSizeAux[j]++;
        }

        j = 0;

        while ((j < this.tamtableBase) && (levelSizeAux[j] != 0)) {
            j++;
        }
        this.nLevels = (byte) j;

        this.levelsIndex = new int[(this.nLevels + 1)];
        bits_BS_len = 0;

        this.base = new int[this.nLevels];
        this.baseBits = new short[this.nLevels];

        for (i = 0; i < this.nLevels; i++) {
            if (i >= nkvalues) {
                this.base[i] = Basics.bitShiftLeftC(1, kvalues[nkvalues - 1]);
                this.baseBits[i] = kvalues[nkvalues - 1];
            } else {
                this.base[i] = Basics.bitShiftLeftC(1, kvalues[i]);
                this.baseBits[i] = kvalues[i];
            }
        }

        int tamLevels = 0;

        tamLevels = 0;
        for (i = 0; i < this.nLevels; i++)
            tamLevels += this.baseBits[i] * levelSizeAux[i];

        this.iniLevel = new int[this.nLevels];
        this.tamCode = tamLevels;

        int indexLevel = 0;
        this.levelsIndex[0] = 0;
        for (j = 0; j < this.nLevels; j++) {
            this.levelsIndex[j + 1] = this.levelsIndex[j] + levelSizeAux[j];
            this.iniLevel[j] = indexLevel;
            cont[j] = this.iniLevel[j];
            indexLevel += levelSizeAux[j] * this.baseBits[j];
            contB[j] = this.levelsIndex[j];

        }

        this.levels = new int[(tamLevels / W + 1)];

        bits_BS_len = this.levelsIndex[this.nLevels - 1] + 1;

        int[] bits_BS = new int[(bits_BS_len / W + 1)];
        for (i = 0; i < ((bits_BS_len) / W + 1); i++)
            bits_BS[i] = 0;
        for (i = 0; i < listLength; i++) {
            value = list[i];
            j = this.nLevels - 1;

            while (j >= 0) {
                if (value >= this.tableBase[j]) {

                    newvalue = value - this.tableBase[j];

                    for (k = 0; k < j; k++) {

                        bitwrite(this.levels, cont[k], this.baseBits[k], newvalue % this.base[k]);
                        cont[k] += this.baseBits[k];
                        contB[k]++;

                        newvalue = newvalue / this.base[k];
                    }
                    k = j;

                    bitwrite(this.levels, cont[j], this.baseBits[j], newvalue % this.base[j]);
                    cont[j] += this.baseBits[j];
                    contB[j]++;
                    if (j < this.nLevels - 1) {
                        bitset(bits_BS, contB[j] - 1);

                    }

                    break;
                }
                j--;
            }

        }

        bitset(bits_BS, bits_BS_len - 1);

        this.bS = new BitRank(bits_BS, bits_BS_len, (char) 1, 20);

        this.rankLevels = new int[this.nLevels];
        for (j = 0; j < this.nLevels; j++)
            this.rankLevels[j] = this.bS.rank(this.levelsIndex[j] - 1);
    }

    private static short[] optimize2(int[] list) {
        int listLength = list.length;
        int t, i, m, k;

        int maxInt = 0;
        // find max integer in list
        for (i = 0; i < listLength; i++)
            if (maxInt < list[i])
                maxInt = list[i];

        int nBits = bits(maxInt) - 1;
        // if (nBits == -1) nBits++;

        int tamAux = nBits + 2;

        int[] weight = new int[maxInt + 1];

        for (i = 0; i < maxInt + 1; i++)
            weight[i] = 0;

        // count the number of times each integer appears in the list
        for (i = 0; i < listLength; i++)
            weight[list[i]]++;

        // prefix-sum array of the weight array
        int[] acumFreq = new int[tamAux];

        // compute prefix sum array
        int acumValue = 0;
        acumFreq[0] = 0;
        int cntb = 1;
        for (i = 0; i < maxInt + 1; i++) {
            if (i == (Basics.bitShiftLeftC(1, cntb))) {
                acumFreq[cntb] = acumValue;
                cntb++;
            }

            acumValue += weight[i];

        }

        acumFreq[cntb] = listLength;

        long[] s = new long[nBits + 1];
        int[] l = new int[nBits + 1];
        int[] b = new int[nBits + 1];

        long currentSize;

        m = nBits;

        s[m] = 0;
        l[m] = 0;
        b[m] = 0;

        int[] fc = acumFreq;

        long minSize;
        int minPos;
        for (t = m; t >= 0; t--) {
            minSize = Long.MAX_VALUE;
            minPos = m;
            for (i = m; i >= t + 1; i--) {
                currentSize = s[i] + (fc[m + 1] - fc[t]) * (i - t + 1) + (fc[m + 1] - fc[t]) / FACT_RANK;
                if (minSize > currentSize) {
                    minSize = currentSize;
                    minPos = i;
                }
            }
            if (minSize < ((fc[m + 1] - fc[t]) * (m - t + 1))) {
                s[t] = minSize;
                l[t] = l[minPos] + 1;
                b[t] = minPos - t;

            } else {
                s[t] = (fc[m + 1] - fc[t]) * (m - t + 1);
                l[t] = 1;
                b[t] = m - t + 1;
            }

        }

        int L = l[0];

        short[] kvalues = new short[L];

        t = 0;
        for (k = 0; k < L; k++) {
            kvalues[k] = (short) b[t];
            t = t + b[t];
        }

        return kvalues;
    }

    public int accessFT(int param) {
        int mult = 0;
        int j;
        int partialSum = 0;
        int ini = param - 1;
        int nLevels = this.nLevels;
        int[] level;
        int readByte;
        int cont, pos, rankini;

        partialSum = 0;
        j = 0;
        level = this.levels;

        pos = this.levelsIndex[j] + ini;

        mult = 0;

        cont = this.iniLevel[j] + ini * this.baseBits[j];

        readByte = bitread(level, cont, this.baseBits[j]);
        if (nLevels == 1) {
            return readByte;
        }
        while ((bitget(this.bS.data, pos)) == 0) {

            rankini = this.bS.rank(this.levelsIndex[j] + ini - 1) - this.rankLevels[j];
            ini = ini - rankini;
            partialSum = partialSum + Basics.bitShiftLeftC(readByte, mult);

            mult += this.baseBits[j];
            j++;

            cont = this.iniLevel[j] + ini * this.baseBits[j];
            pos = this.levelsIndex[j] + ini;

            readByte = bitread(level, cont, this.baseBits[j]);

            if (j == nLevels - 1) {
                break;
            }

        }
        partialSum = partialSum + (Basics.bitShiftLeftC(readByte, mult)) + this.tableBase[j];

        return partialSum;

    }

}
