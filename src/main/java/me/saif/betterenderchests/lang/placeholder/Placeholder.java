package me.saif.betterenderchests.lang.placeholder;

import java.util.function.Function;

public abstract class Placeholder<E> {

    public static final char PLACEHOLDER_PREFIX = '<';
    public static final char PLACEHOLDER_SUFFIX = '>';

    private final String placeholder;

    public Placeholder(String placeholder) {
        this.placeholder = PLACEHOLDER_PREFIX + placeholder + PLACEHOLDER_SUFFIX;
    }

    public abstract String getValue(E e);

    public final PlaceholderResult getResult(E toParse) {
        return new PlaceholderResult(this.placeholder, this.getValue(toParse));
    }

    public static <F> Placeholder<F> getPlaceholder(String placeholder, Function<F,String> function) {
        return new Placeholder<F>(placeholder) {
            @Override
            public String getValue(F f) {
                return function.apply(f);
            }
        };
    }

    public static Placeholder<String> getStringPlaceholder(String placeholder) {
        return new Placeholder<String>(placeholder) {
            @Override
            public String getValue(String s) {
                return s;
            }
        };
    }

}
