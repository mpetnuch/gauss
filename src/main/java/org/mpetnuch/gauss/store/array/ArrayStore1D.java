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

import java.util.Arrays;
import java.util.Spliterator;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class ArrayStore1D extends ArrayStore implements Store1D {
    private final ArrayStructure1D structure;

    public ArrayStore1D(double[] array, ArrayStructure1D structure) {
        super(array);

        this.structure = structure;
    }

    public ArrayStore1D from(double[] array) {
        return new ArrayStore1D(array, new ArrayStructure1D(array.length, 1, 0));
    }

    @Override
    public double get(int index) {
        return array[structure.index(index)];
    }

    @Override
    public ArrayStructure1D getStructure() {
        return structure;
    }

    public ArrayStore1D slice(int startIndex, int endIndex) {
        return null;
    }

    @Override
    public ArrayStore2D reshape(int rowCount, int columnCount) {
        return null;
    }

    @Override
    public ArrayStore1D compact() {
        if (structure.offset == 0 && structure.stride == 1) {
            return this;
        }

        return new ArrayStore1D(toArray(), structure.compact());
    }

    @Override
    public void copyInto(double[] copy, int offset) {
        if (structure.stride == 1) {
            System.arraycopy(array, structure.offset, copy, offset, structure.size);
            return;
        }

        final int fence = structure.offset + structure.size * structure.stride;
        // else we need to access the array in a strided fashion to copy into the requested array
        for (int i = structure.offset, k = offset; i < fence; i += structure.stride) {
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
    }
}
