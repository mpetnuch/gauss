/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStore1D.java` is part of Gauss.
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

import org.mpetnuch.gauss.store.Store1D;
import org.mpetnuch.gauss.store.array.ArrayStore2D.ArrayStructure2D;
import org.mpetnuch.gauss.store.array.ArrayStore2D.RowMajorArrayStructure2D;

import java.util.Arrays;
import java.util.Spliterator;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class ArrayStore1D extends ArrayStore<ArrayStore1D.ArrayStructure1D> implements Store1D {
    public ArrayStore1D(double[] array, ArrayStructure1D structure) {
        super(array, structure);
    }

    @Override
    public double get(int index) {
        return array[structure.index(index)];
    }

    public ArrayStore1D slice(int startInclusive, int endExclusive) {
        return new ArrayStore1D(array, structure.slice(startInclusive, endExclusive));
    }

    @Override
    public ArrayStore2D reshape(int rowCount, int columnCount) {
        return new ArrayStore2D(array, structure.reshape(rowCount, columnCount));
    }

    public ArrayStore1D compact() {
        if (structure.isCompact() && array.length == size()) {
            return this;
        } else {
            return new ArrayStore1D(toArray(), structure.compact());
        }
    }

    @Override
    public void copyInto(double[] copy, int offset) {
        if (structure.stride == 1) {
            System.arraycopy(array, structure.index(0), copy, offset, size());
            return;
        }

        final int fence = structure.index(size());
        // else we need to access the array in a strided fashion to copy into the requested array
        for (int i = structure.index(0), k = offset; i < fence; i += structure.stride) {
            copy[k++] = array[i];
        }
    }

    @Override
    public Spliterator.OfDouble spliterator() {
        if (structure.stride == 1) {
            return Arrays.spliterator(array, structure.index(0), structure.index(size()));
        } else {
            return new StridedArrayStructureSpliterator(structure, array);
        }
    }

    public static class ArrayStructure1D extends ArrayStructure {
        final int stride;
        final int length;

        public ArrayStructure1D(int length, int stride, int offset) {
            super(new int[]{length}, new int[]{stride}, offset);
            this.stride = stride;
            this.length = length;
        }

        public ArrayStructure1D(int length) {
            this(length, 1, 0);
        }

        public int index(int index) {
            return offset + index * stride;
        }

        @Override
        public int dimension() {
            return 1;
        }

        public ArrayStructure1D compact() {
            return new ArrayStructure1D(length);
        }

        public ArrayStructure2D reshape(int rowCount, int columnCount) {
            return new RowMajorArrayStructure2D(rowCount, columnCount, stride, offset);
        }

        public ArrayStructure1D slice(int startInclusive, int endExclusive) {
            return new ArrayStructure1D(endExclusive - startInclusive, stride, index(startInclusive));
        }
    }
}
