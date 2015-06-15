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

import org.mpetnuch.gauss.exception.DimensionMismatchException;
import org.mpetnuch.gauss.store.DataFlag;
import org.mpetnuch.gauss.store.Store1D;
import org.mpetnuch.gauss.structure.Slice;
import org.mpetnuch.gauss.structure.array.ArrayStructure1D;
import org.mpetnuch.gauss.structure.array.spliterator.ArrayStructureSpliterator;
import org.mpetnuch.gauss.structure.array.spliterator.NaturalOrderSpliterator;

import java.util.EnumSet;
import java.util.Set;

import static org.mpetnuch.gauss.structure.Slice.S;

/**
 * @author Michael Petnuch
 */
public class ArrayStore1D implements ArrayStore, Store1D {
    private final Set<DataFlag> flags = EnumSet.noneOf(DataFlag.class);
    private final ArrayStructure1D structure;
    private final double[] array;

    public ArrayStore1D(double[] array, ArrayStructure1D structure) {
        this.array = array;
        this.structure = structure;
    }

    @Override
    public Set<DataFlag> flags() {
        return flags;
    }

    @Override
    public ArrayStore1D compact() {
        return new ArrayStore1D(toArray(), new ArrayStructure1D(length()));
    }

    @Override
    public double get(int index) {
        return array[structure.index(index)];
    }


    @Override
    public ArrayStore1D slice(Slice slice) {
        return new ArrayStore1D(array, structure.slice(slice));
    }

    @Override
    public ArrayStore1D slice(int startInclusive, int endExclusive) {
        return new ArrayStore1D(array, structure.slice(S(startInclusive, endExclusive)));
    }

    @Override
    public ArrayStore reshape(int... dimensions) {
        switch (dimensions.length) {
            case 2:
                return reshape(dimensions[0], dimensions[1]);
            case 1:
                return reshape(dimensions[0]);
        }

        return new ArrayStoreAnyD(array, structure.reshape(dimensions));
    }

    @Override
    public ArrayStore2D reshape(int rowCount, int columnCount) {
        return new ArrayStore2D(array, structure.reshape(rowCount, columnCount));
    }

    @Override
    public ArrayStore1D reshape(int length) {
        if (length() == length) {
            return this;
        }

        throw new DimensionMismatchException(length(), length);
    }

    @Override
    public ArrayStore1D slice(Slice... slices) {
        return new ArrayStore1D(array, structure.slice(slices));
    }

    @Override
    public ArrayStore1D swapAxis(int axis1, int axis2) {
        return new ArrayStore1D(array, structure.swapAxis(axis1, axis2));
    }

    @Override
    public ArrayStructure1D structure() {
        return structure;
    }

    @Override
    public double get(int... indices) {
        return array[structure.index(indices[0])];
    }

    @Override
    public ArrayStructureSpliterator spliterator() {
        return new NaturalOrderSpliterator(structure, array);
    }
}
