package dk.itu.raven.join.results;

import java.util.List;

public interface IResultCreator {
    public void createResults(PixelRange range, long value, List<IResult> out);

    public boolean hasValues();
}
