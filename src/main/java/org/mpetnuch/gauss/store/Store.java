package org.mpetnuch.gauss.store;

import java.util.stream.DoubleStream;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public interface Store extends Iterable<Double> {
    DoubleStream stream();

    int size();
}
