/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStoreAnyD.java` is part of Gauss.
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

import org.mpetnuch.gauss.store.array.ArrayStructureSpliterator.GeneralArrayStructureSpliterator;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.stream.IntStream;

/**
 * @author Michael Petnuch
 */
public class ArrayStoreAnyD extends ArrayStore<ArrayStoreAnyD.ArrayStructureAnyD> {

    protected ArrayStoreAnyD(double[] array, ArrayStructureAnyD structure) {
        super(array, structure);
    }

    @Override
    public void copyInto(double[] copy, int offset) {
        final PrimitiveIterator.OfDouble spliterator = iterator();
        while (spliterator.hasNext()) {
            copy[offset++] = spliterator.nextDouble();
        }
    }

    @Override
    public Spliterator.OfDouble spliterator() {
        return new GeneralArrayStructureSpliterator(structure, array);
    }

    public static class ArrayStructureAnyD implements ArrayStructure {
        private final int dimension;
        private final int size;
        private final int offset;
        private final int lastIndex;
        private final int unitStrideDimension;
        private final boolean contiguous;
        private final int[] dimensions, strides, backstrides, factors;

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

            this.backstrides = product(strides, dimensionm1).toArray();

            this.lastIndex = offset + product(dimensionm1, strides).sum();

            this.unitStrideDimension = IntStream.range(0, dimension).
                    filter(value -> strides[value] == 1).
                    findFirst().orElse(ArrayStructure.NO_UNIT_STRIDE_DIMENSION);

            this.contiguous = isContiguous(dimensions, strides, dimension);
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

        private static IntStream product(int[] a, int[] b) {
            if (a.length != b.length) {
                throw new IllegalArgumentException();
            }

            return IntStream.range(0, a.length).map(n -> a[n] * b[n]);
        }

        @Override
        public int index(int... indices) {
            return offset + product(indices, strides).sum();
        }

        @Override
        public int lastIndex() {
            return lastIndex;
        }

        public int[] indicies(int ordinal) {
            final int[] indicies = new int[dimension];

            indicies[0] = ordinal;
            for (int i = 1; i < dimension; i++) {
                indicies[i] = indicies[i - 1] % factors[i - 1];
            }

            for (int i = 0; i < dimension; i++) {
                indicies[i] = Math.floorDiv(indicies[i], factors[i]);
            }

            return indicies;
        }

        @Override
        public int ordinal(int[] indicies) {
            return IntStream.range(0, dimension).
                    reduce(0, (sum, i) -> sum + indicies[i] * factors[i]);
        }

        @Override
        public int index(int ordinal) {
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

        @Override
        public int offset() {
            return offset;
        }

        @Override
        public int stride(int dimension) {
            return strides[dimension];
        }

        @Override
        public int backstride(int dimension) {
            return backstrides[dimension];
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
            return dimensions[dimension];
        }

        @Override
        public int size() {
            return size;
        }
    }
}
