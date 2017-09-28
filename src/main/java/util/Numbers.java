package util;

import java.util.function.Predicate;

public class Numbers {
    public static <T extends Number> T require(T number, Predicate<T> statement) {

        if (!statement.test(number))
            throw new IllegalArgumentException();

        return number;

    }

}
