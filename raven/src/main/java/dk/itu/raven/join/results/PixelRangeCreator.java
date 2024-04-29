package dk.itu.raven.join.results;

import java.util.List;

public class PixelRangeCreator implements IResultCreator {

    @Override
    public void createResults(PixelRange range, long value, List<IResult> out) {
        out.add(range);
    }

    @Override
    public boolean hasValues() {
        return false;
    }
}
