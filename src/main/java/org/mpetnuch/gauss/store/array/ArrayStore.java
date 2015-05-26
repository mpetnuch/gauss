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
import org.mpetnuch.gauss.store.Structure;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.Spliterators;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public abstract class ArrayStore implements StoreAnyD {
    protected final double[] array;

    protected ArrayStore(double[] array) {
        this.array = array;
    }

    public abstract ArrayStructure getStructure();

    public abstract ArrayStore compact();

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
        return getStructure().dimensionLength(dimension);
    }

    @Override
    public final int size() {
        return getStructure().size();
    }

    @Override
    public double get(int... indices) {
        if (getStructure().dimension() != indices.length) {
            throw new IllegalArgumentException();
        }

        return array[getStructure().index(indices)];
    }

    @Override
    public PrimitiveIterator.OfDouble iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public DoubleStream stream() {
        return StreamSupport.doubleStream(spliterator(), false);
    }

    protected abstract static class ArrayStructure implements Structure {
        protected final int dimension;
        protected final int size;
        protected final int offset;
        protected final int[] dimensions, strides;

        public ArrayStructure(int[] dimensions, int[] strides, int offset) {
            this.offset = offset;
            this.strides = strides;
            this.dimension = dimensions.length;
            this.dimensions = dimensions;
            this.size = Arrays.stream(dimensions).reduce(1, (left, right) -> left * right);
        }

        public int index(int... indices) {
            int index = offset;
            for (int n = 0; n < dimension; n++) {
                index += indices[n] * strides[n];
            }

            return index;
        }

        public int getOffset() {
            return offset;
        }

        public int getStrides(int dimension) {
            return strides[dimension];
        }

        @Override
        public int dimension() {
            return dimension;
        }

        @Override
        public int dimensionLength(int dimension) {
            return dimensions[dimension];
        }

        @Override
        public int size() {
            return size;
        }
    }
}
