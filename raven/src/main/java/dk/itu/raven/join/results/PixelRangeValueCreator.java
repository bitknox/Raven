package dk.itu.raven.join.results;

import java.util.List;

public class PixelRangeValueCreator implements IResultCreator {

    @Override
    public void createResults(PixelRange range, long value, List<IResult> out) {
        out.add(new PixelRangeValue(range.row, range.x1, range.x2, value));
    }

    @Override
    public boolean hasValues() {
        return true;
    }

}
