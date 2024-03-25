package dk.itu.raven.ksquared.dac;

import dk.itu.raven.util.IntArrayWrapper;
import dk.itu.raven.util.PrimitiveArrayWrapper;

public class IntDAC extends AbstractDAC {

    public IntDAC(PrimitiveArrayWrapper values) {
        super(values);
    }

    @Override
    protected PrimitiveArrayWrapper getWrapper(int size) {
        return new IntArrayWrapper(new int[size]);
    }
}
