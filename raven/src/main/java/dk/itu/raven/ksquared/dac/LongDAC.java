package dk.itu.raven.ksquared.dac;

import dk.itu.raven.util.LongArrayWrapper;
import dk.itu.raven.util.PrimitiveArrayWrapper;

public class LongDAC extends AbstractDAC {

    public LongDAC(PrimitiveArrayWrapper values) {
        super(values);
    }

    @Override
    protected PrimitiveArrayWrapper getWrapper(int size) {
        return new LongArrayWrapper(new long[size]);
    }
}
