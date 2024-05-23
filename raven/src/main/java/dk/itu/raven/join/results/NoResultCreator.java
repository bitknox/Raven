package dk.itu.raven.join.results;

import java.util.List;

public class NoResultCreator implements IResultCreator {

    @Override
    public void createResults(PixelRange range, long value, List<IResult> out) {
        // do nothing
    }

    @Override
    public boolean hasValues() {
        return true;
    }

}
