/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStore.java` is part of Gauss.
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

package org.mpetnuch.gauss.store.array;

import org.mpetnuch.gauss.store.StoreAnyD;

import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterators;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public abstract class ArrayStore<Structure extends ArrayStructure> implements StoreAnyD {
    protected final double[] array;
    protected final Structure structure;

    protected ArrayStore(double[] array, Structure structure) {
        Objects.requireNonNull(array, "Array cannot be null");
        Objects.requireNonNull(structure, "Structure cannot be null");

        if (structure.size() > array.length) {
            throw new IllegalArgumentException("Structure is not compatible with array size");
        }

        this.structure = structure;
        this.array = array;
    }

    public Structure structure() {
        return structure;
    }

    public abstract void copyInto(double[] copy, int offset);

    public void copyInto(double[] copy) {
        copyInto(copy, 0);
    }

    public double[] toArray() {
        final double[] copy = new double[size()];
        copyInto(copy);
        return copy;
    }

    @Override
    public final int dimension(int dimension) {
        return structure.dimensionLength(dimension);
    }

    @Override
    public final int size() {
        return structure.size();
    }

    @Override
    public double get(int... indices) {
        if (structure.dimension() != indices.length) {
            throw new IllegalArgumentException();
        }

        return array[structure.index(indices)];
    }

    @Override
    public PrimitiveIterator.OfDouble iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public DoubleStream stream() {
        return StreamSupport.doubleStream(spliterator(), false);
    }
}
