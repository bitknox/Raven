package dk.itu.raven.io.commandline;

import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.converters.IParameterSplitter;

public class RangeSplitter implements IParameterSplitter {

    @Override
    public List<String> split(String value) {
        return Arrays.asList(value.split("[,-]"));
    }
    
}
