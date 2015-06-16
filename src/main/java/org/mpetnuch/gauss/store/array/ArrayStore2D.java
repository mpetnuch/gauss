/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStore2D.java` is part of Gauss.
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
import org.mpetnuch.gauss.exception.InvalidShapeException;
import org.mpetnuch.gauss.store.DataFlag;
import org.mpetnuch.gauss.store.Store2D;
import org.mpetnuch.gauss.structure.Slice;
import org.mpetnuch.gauss.structure.array.ArrayStructure1D;
import org.mpetnuch.gauss.structure.array.ArrayStructure2D;
import org.mpetnuch.gauss.structure.array.ArrayStructureAnyD;
import org.mpetnuch.gauss.structure.array.spliterator.ArrayStructureSpliterator;
import org.mpetnuch.gauss.structure.array.spliterator.NaturalOrderSpliterator;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;

import static org.mpetnuch.gauss.structure.Slice.S;

/**
 * @author Michael Petnuch
 */
public class ArrayStore2D implements ArrayStore, Store2D {
    private final ArrayStructure2D structure;
    private final Set<DataFlag> flags;
    private final double[] array;

    public ArrayStore2D(int rowCount, int columnCount, DataFlag... flags) {
        this.array = new double[rowCount * columnCount];
        this.structure = new ArrayStructure2D(rowCount, columnCount);
        this.flags = EnumSet.of(DataFlag.Writable, DataFlag.Contiguous);
    }

    public ArrayStore2D(double[] array, ArrayStructure2D structure) {
        if (array.length < structure.size()) {
            throw new IllegalArgumentException("Array is incompatible with structure");
        }

        this.array = array;
        this.structure = structure;
        this.flags = EnumSet.noneOf(DataFlag.class);
    }

    @Override
    public Set<DataFlag> flags() {
        return flags;
    }

    @Override
    public ArrayStructure2D structure() {
        return structure;
    }

    @Override
    public ArrayStore2D compact() {
        return new ArrayStore2D(toArray(), new ArrayStructure2D(rowCount(), columnCount()));
    }

    @Override
    public int rowCount() {
        return structure.rowCount();
    }

    @Override
    public int columnCount() {
        return structure.columnCount();
    }

    @Override
    public double get(int... indices) {
        if (structure().dimension() != indices.length) {
            throw new IllegalArgumentException();
        }

        return get(indices[0], indices[1]);
    }

    @Override
    public double get(int rowIndex, int columnIndex) {
        return array[structure.index(rowIndex, columnIndex)];
    }

    @Override
    public ArrayStore1D column(int columnIndex) {
        return new ArrayStore1D(array, structure.column(columnIndex));
    }

    @Override
    public ArrayStore1D row(int rowIndex) {
        return new ArrayStore1D(array, structure.row(rowIndex));
    }

    @Override
    public ArrayStore2D slice(Slice... slices) {
        return new ArrayStore2D(array, structure.slice(slices));
    }

    @Override
    public Store2D slice(Slice rowSlice, Slice columnSlice) {
        return new ArrayStore2D(array, structure.slice(rowSlice, columnSlice));
    }

    @Override
    public ArrayStore2D slice(int rowStartInclusive, int rowEndExclusive,
                              int columnStartInclusive, int columnEndExclusive) {
        return new ArrayStore2D(array,
                structure.slice(S(rowStartInclusive, rowEndExclusive), S(columnStartInclusive, columnEndExclusive)));
    }

    @Override
    public ArrayStore1D reshape(int length) {
        if (size() != length) {
            throw new DimensionMismatchException(length, size());
        }

        final int offset = structure.offset();
        final int rowStride = structure.rowStride();
        final int columnStride = structure.columnStride();

        if (columnStride * columnCount() == rowStride) {
            // then we can directly reshape the array with out making a copy
            final ArrayStructure1D reshapedStructure = new ArrayStructure1D(size(), columnStride, offset);
            return new ArrayStore1D(array, reshapedStructure);
        } else {
            // impossible, need to make a copy
            final ArrayStructure1D reshapedStructure = new ArrayStructure1D(size());
            return new ArrayStore1D(toArray(), reshapedStructure);
        }
    }

    @Override
    public ArrayStore2D reshape(int rowCount, int columnCount) {
        if (size() != rowCount * columnCount) {
            throw new InvalidShapeException(new int[]{rowCount, columnCount}, new int[]{rowCount(), columnCount()});
        }

        final int rowStride = structure.rowStride();
        final int columnStride = structure.columnStride();

        if (rowStride != columnStride * columnCount()) {
            // need to make a copy, can't be handled with a single stride
            final ArrayStructure2D reshapedStructure = new ArrayStructure2D(rowCount, columnCount);
            return new ArrayStore2D(toArray(), reshapedStructure);
        }

        // note: rowStride == columnStride * column() => offset == 0
        final int offset = 0;

        if (columnStride == 1) {
            final ArrayStructure2D reshapedStructure = new ArrayStructure2D(rowCount, columnCount, offset);
            return new ArrayStore2D(array, reshapedStructure);
        }

        if (columnCount() % columnCount == 0) {
            // the column stride is staying the same, but we need to preserve rowStride == columnStride * columnCount;
            // note however columnCount is the *new* reshaped columnCount
            final int reshapedRowStride = columnStride * columnCount;
            final ArrayStructure2D reshapedStructure =
                    new ArrayStructure2D(rowCount, reshapedRowStride, columnCount, columnStride, offset);
            return new ArrayStore2D(array, reshapedStructure);
        }

        // need to make a copy, can't be handled with a single stride
        final ArrayStructure2D reshapedStructure = new ArrayStructure2D(rowCount, columnCount);
        return new ArrayStore2D(toArray(), reshapedStructure);
    }

    @Override
    public ArrayStore reshape(int... dimensions) {
        switch (dimensions.length) {
            case 2:
                return reshape(dimensions[0], dimensions[1]);
            case 1:
                return reshape(dimensions[0]);
        }

        // I haven't had time to think about when it's possible to do this without a copy so for now, we will
        // always make a copy
        final ArrayStructureAnyD reshapedStructure = new ArrayStructureAnyD(dimensions);
        return new ArrayStoreAnyD(toArray(), reshapedStructure);
    }

    @Override
    public ArrayStore2D transpose() {
        return new ArrayStore2D(array, structure.transpose());
    }

    @Override
    public ArrayStore2D swapAxis(int axis1, int axis2) {
        return new ArrayStore2D(array, structure.swapAxis(axis1, axis2));
    }

    @Override
    public ArrayStructureSpliterator spliterator() {
        return new NaturalOrderSpliterator(structure, array);
    }

    public ArrayStore2D immutableCopy() {
        return new ArrayStore2D(array.clone(), structure);
    }

    public void increment(int rowIndex, int columnIndex, double x) {
        if (!flags.contains(DataFlag.Writable)) {
            throw new IllegalStateException("ArrayStore2D is not writable");
        }

        array[structure.index(rowIndex, columnIndex)] += x;
    }

    public void set(int rowIndex, int columnIndex, double x) {
        if (!flags.contains(DataFlag.Writable)) {
            throw new IllegalStateException("ArrayStore2D is not writable");
        }

        array[structure.index(rowIndex, columnIndex)] = x;
    }

    public void replaceAll(DoubleUnaryOperator operator) {
        if (!flags.contains(DataFlag.Writable)) {
            throw new IllegalStateException("ArrayStore2D is not writable");
        }

        Arrays.setAll(array, operator::applyAsDouble);
    }
}
