package org.mpetnuch.gauss.store;

import java.util.PrimitiveIterator;
import java.util.stream.DoubleStream;

/**
 * @author Michael Petnuch
 * @id $Id$
 */
public interface Structure {
    int dimensionLength(int dimension);

    int dimension();

    int size();

    PrimitiveIterator.OfDouble iterator(double[] array);

    DoubleStream stream(double[] array);

    double get(double[] array, int... indices);
}
