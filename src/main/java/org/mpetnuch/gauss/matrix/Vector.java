package org.mpetnuch.gauss.matrix;

import java.io.Serializable;
import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.stream.DoubleStream;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public interface Vector extends Serializable {
    double get(int i);

    int length();

    Matrix reshape(int rows, int columns);

    Vector slice(int start, int end);

    Vector compact();

    DoubleStream stream();

    default Matrix toColumnMatrix() {
        return reshape(length(), 1);
    }

    default Matrix toRowMatrix() {
        return reshape(1, length());
    }

    default double[] toArray() {
        return stream().toArray();
    }

    default void toArray(double[] array) {
        PrimitiveIterator.OfDouble values = stream().iterator();
        Arrays.setAll(array, value -> values.next());
    }
}
