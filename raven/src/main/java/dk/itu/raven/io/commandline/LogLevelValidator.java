package dk.itu.raven.io.commandline;

import java.util.HashSet;
import java.util.Set;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

import dk.itu.raven.util.Logger;

public class LogLevelValidator implements IParameterValidator {
    private static Set<String> valid = new HashSet<>();
    private static String validStrings = "";
    static {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Logger.LogLevel lvl : Logger.LogLevel.values()) {
            valid.add(lvl.name());
            sb.append(lvl.name());
            if (++i < Logger.LogLevel.values().length) {
                sb.append(", ");
            }
        }

        validStrings = sb.toString();
    }
    @Override
    public void validate(String name, String value) throws ParameterException {
        if (!valid.contains(value)) {
            throw new ParameterException("Parameter " + name + " should be one of [" + validStrings + "] (found " + value + ")");
        }
    }
    
}
