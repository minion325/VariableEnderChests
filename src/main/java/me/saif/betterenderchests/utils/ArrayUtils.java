package me.saif.betterenderchests.utils;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtils {

    public static <E> List<E> concatenate(E[]... arrays) {
        int length = 0;
        for (E[] array : arrays) {
            length += array.length;
        }

        List<E> elements = new ArrayList<>(length);

        int currentArray = 0;
        int mainIndex = 0;
        while (mainIndex < length) {
            E[] arrayToLoop = arrays[currentArray];

            for (E e : arrayToLoop) {
                elements.add(mainIndex, e);
                mainIndex++;
            }

            currentArray++;
        }

        return elements;
    }

}
