/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `Store.java` is part of Gauss.
 *
 * Gauss is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.mpetnuch.gauss.store;

import org.mpetnuch.gauss.structure.Slice;
import org.mpetnuch.gauss.structure.Structure;

import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.DoubleConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public interface Store extends Iterable<Double> {
    Set<DataFlag> flags();

    double get(int... indices);

    Store swapAxis(int axis1, int axis2);

    Store reshape(int... dimensions);

    Store1D reshape(int length);

    Store2D reshape(int length, int width);

    Store slice(Slice... slices);

    Structure structure();

    default int dimensionLength(int dimension) {
        return structure().dimensionLength(dimension);
    }

    default int size() {
        return structure().size();
    }

    @Override
    Spliterator.OfDouble spliterator();

    @Override
    default PrimitiveIterator.OfDouble iterator() {
        return Spliterators.iterator(spliterator());
    }

    default DoubleStream stream() {
        return StreamSupport.doubleStream(spliterator(), false);
    }

    default DoubleStream parallelStream() {
        return StreamSupport.doubleStream(spliterator(), true);
    }

    default void forEach(DoubleConsumer action) {
        spliterator().forEachRemaining(action);
    }

    default double[] toArray() {
        return stream().toArray();
    }
}
