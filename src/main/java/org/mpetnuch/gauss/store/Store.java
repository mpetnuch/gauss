package org.mpetnuch.gauss.store;

import java.util.stream.DoubleStream;

/**
 * @author Michael Petnuch
 * @id $Id$
 */
public interface Store extends Iterable<Double> {
    DoubleStream stream();

    int size();
}
