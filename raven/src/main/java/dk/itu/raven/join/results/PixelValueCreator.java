package dk.itu.raven.join.results;

import java.util.List;

public class PixelValueCreator implements IResultCreator {

    @Override
    public void createResults(PixelRange range, long value, List<IResult> out) {
        for (int x = range.x1; x <= range.x2; x++) {
            out.add(new PixelValue(value, x, range.row));
        }
    }

    @Override
    public boolean hasValues() {
        return true;
    }

}
