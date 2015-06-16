/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStructureAnyD.java` is part of Gauss.
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

package org.mpetnuch.gauss.structure.array;

import org.mpetnuch.gauss.exception.DimensionMismatchException;
import org.mpetnuch.gauss.exception.InvalidRangeException;
import org.mpetnuch.gauss.misc.MathUtils;
import org.mpetnuch.gauss.structure.Slice;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.mpetnuch.gauss.structure.Slice.All;

/**
 * @author Michael Petnuch
 */
public class ArrayStructureAnyD implements ArrayStructure {
    private final int dimension;
    private final int size;
    private final int offset;
    private final int lastIndex;
    private final int unitStrideDimension;
    private final boolean contiguous;
    private final int[] dimensions, strides, backstrides, factors;

    public ArrayStructureAnyD(int[] dimensions) {
        this(dimensions, computeFactors(dimensions), 0);
    }

    public ArrayStructureAnyD(int[] dimensions, int[] strides, int offset) {
        this.offset = offset;
        this.strides = strides;
        this.dimension = dimensions.length;
        this.dimensions = dimensions;

        this.factors = computeFactors(dimensions);

        this.size = Arrays.stream(dimensions).
                reduce(1, (product, n) -> product * n);

        final int[] dimensionm1 = Arrays.stream(dimensions).
                map(n -> n - 1).toArray();

        this.backstrides = MathUtils.product(strides, dimensionm1).toArray();

        this.lastIndex = offset + MathUtils.product(dimensionm1, strides).sum();

        this.unitStrideDimension = IntStream.range(0, dimension).
                filter(value -> strides[value] == 1).
                findFirst().orElse(ArrayStructure.NO_UNIT_STRIDE_DIMENSION);

        this.contiguous = isContiguous(dimensions, strides, dimension);
    }

    public static ArrayStructureAnyD from(ArrayStructure structure) {
        if (structure instanceof ArrayStructureAnyD) {
            return (ArrayStructureAnyD) structure;
        }

        final int dimension = structure.dimension();
        final int[] strides = new int[dimension];
        final int[] dimensions = new int[dimension];
        for (int i = 0; i < dimension; i++) {
            strides[i] = structure.stride(i);
            dimensions[i] = structure.dimensionLength(i);
        }

        return new ArrayStructureAnyD(dimensions, strides, structure.offset());
    }

    private static boolean isContiguous(int[] dimensions, int[] strides, int dimension) {
        return false;
    }

    private static int[] computeFactors(int[] dimensions) {
        final int n = dimensions.length;
        final int[] factors = new int[n];

        factors[n - 1] = 1;
        for (int i = n - 2; i >= 0; i--) {
            factors[i] = dimensions[i] * factors[i + 1];
        }

        return factors;
    }

    @Override
    public int index(int... indices) {
        if (indices.length != dimension) {
            throw new DimensionMismatchException(indices.length, dimension);
        }

        return offset + MathUtils.product(indices, strides).sum();
    }

    @Override
    public int lastIndex() {
        return lastIndex;
    }

    public int[] indices(int relativeOrdinal) {
        final int ordinal = relativeOrdinal < 0 ? relativeOrdinal + size : relativeOrdinal;
        if (ordinal >= size) {
            throw new InvalidRangeException(relativeOrdinal, 0, size);
        }

        final int[] indices = new int[dimension];

        indices[0] = ordinal;
        for (int i = 1; i < dimension; i++) {
            indices[i] = indices[i - 1] % factors[i - 1];
        }

        for (int i = 0; i < dimension; i++) {
            indices[i] = Math.floorDiv(indices[i], factors[i]);
        }

        return indices;
    }

    @Override
    public int ordinal(int... indices) {
        if (indices.length != dimension) {
            throw new DimensionMismatchException(indices.length, dimension);
        }

        return IntStream.range(0, dimension).
                reduce(0, (sum, i) -> sum + indices[i] * factors[i]);
    }

    @Override
    public int index(int relativeOrdinal) {
        final int ordinal = relativeOrdinal < 0 ? relativeOrdinal + size : relativeOrdinal;
        if (ordinal >= size) {
            throw new InvalidRangeException(relativeOrdinal, 0, size);
        }

        final int[] l = new int[dimension];
        l[0] = ordinal;
        for (int i = 1; i < dimension; i++) {
            l[i] = l[i - 1] % factors[i - 1];
        }

        int position = offset;
        for (int i = 0; i < dimension; i++) {
            position += Math.floorDiv(l[i], factors[i]) * strides[i];
        }

        return position;
    }

    public ArrayStructureAnyD swapAxis(int axis1, int axis2) {
        final int axis1DimensionIndex = dimension(axis1).dimensionIndex();
        final int axis2DimensionIndex = dimension(axis2).dimensionIndex();
        if (axis1DimensionIndex == axis2DimensionIndex) {
            return this;
        }

        final int[] swappedDimensions = dimensions.clone();
        MathUtils.swap(swappedDimensions, axis1DimensionIndex, axis2DimensionIndex);

        final int[] swappedStrides = strides.clone();
        MathUtils.swap(swappedStrides, axis1DimensionIndex, axis1DimensionIndex);

        return new ArrayStructureAnyD(swappedDimensions, swappedStrides, offset);
    }

    @Override
    public ArrayStructure slice(Slice... slices) {
        if (slices.length > dimension) {
            throw new DimensionMismatchException(slices.length, dimension);
        }

        final int[] sliceIndices = new int[dimension];
        final int[] sliceStrides = new int[dimension];
        final int[] sliceDimensions = new int[dimension];

        dimensions().forEach(dim -> {
            final int dimensionIndex = dim.dimensionIndex();
            // If a slice is not provided for a dimension then assume All();
            final Slice slice = dimensionIndex < slices.length ? slices[dimensionIndex] : All();
            final int width = Math.max(0, slice.stop(dim) - slice.start(dim));

            sliceIndices[dimensionIndex] = slice.start(dim);
            sliceStrides[dimensionIndex] = strides[dimensionIndex] * slice.step();
            sliceDimensions[dimensionIndex] = MathUtils.ceilDiv(width, slice.step());
        });

        return new ArrayStructureAnyD(sliceDimensions, sliceStrides, index(sliceIndices));
    }

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public int stride(int dimension) {
        return strides[dimension(dimension).dimensionIndex()];
    }

    @Override
    public int backstride(int dimension) {
        return backstrides[dimension(dimension).dimensionIndex()];
    }

    @Override
    public boolean isContiguous() {
        return contiguous;
    }

    @Override
    public int unitStrideDimension() {
        return unitStrideDimension;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public int dimensionLength(int dimension) {
        return dimensions[dimension(dimension).dimensionIndex()];
    }

    @Override
    public int size() {
        return size;
    }
}
