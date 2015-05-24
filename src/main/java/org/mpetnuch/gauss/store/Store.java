package org.mpetnuch.gauss.store;

import java.util.PrimitiveIterator;
import java.util.stream.DoubleStream;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public interface Store extends Iterable<Double> {
    PrimitiveIterator.OfDouble iterator();

    DoubleStream stream();

    int size();
}
