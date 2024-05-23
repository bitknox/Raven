package dk.itu.raven;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import dk.itu.raven.io.commandline.ResultType;

public class Util {

    public static Stream<ResultType> getResultTypes() {
        ResultType[] banned = new ResultType[]{ResultType.NONE};
        Set<ResultType> bannedSet = new HashSet<>();
        bannedSet.addAll(Arrays.asList(banned));

        List<ResultType> permitted = new ArrayList<>();
        for (ResultType type : ResultType.values()) {
            if (!bannedSet.contains(type)) {
                permitted.add(type);
            }
        }
        return permitted.stream();
    }
}
