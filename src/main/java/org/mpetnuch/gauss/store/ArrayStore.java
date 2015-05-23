package org.mpetnuch.gauss.store;

import org.mpetnuch.gauss.store.ArrayStore2D.ArrayStructure2D;

import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.*;

/**
 * @author Michael Petnuch
 * @id $Id$
 */
public abstract class ArrayStore implements StoreAnyD {
    protected final double[] array;

    protected ArrayStore(double[] array) {
        this.array = array;
    }

    public static Store2D from(double [][] array) {
        int rowCount = array.length;
        int columnCount = array[0].length;

        ArrayStructure2D structure = new ArrayStore2D.ColumnMajorArrayStructure2D(0, rowCount, columnCount, rowCount);
        return new ArrayStore2D(structure.flatten(array), structure);
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

        return getStructure().get(array, indices);
    }

    @Override
    public DoubleStream stream() {
        return getStructure().stream(array);
    }

    protected static class ArrayStructure implements Structure {
        protected final int dimension;
        protected final int size;
        protected final int offset;
        protected final int[] dimensions, strides;

        public ArrayStructure(int offset, int[] dimensions, int[] strides) {
            this.offset = offset;
            this.strides = strides;
            this.dimension = dimensions.length;
            this.dimensions = dimensions;
            this.size = Arrays.stream(dimensions).reduce(1, (left, right) -> left * right);
        }

        private int getIndex(int... indices) {
            int index = offset;
            for (int n = 0; n < dimension; n++) {
                index += indices[n] * strides[n];
            }

            return index;
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

        @Override
        public double get(double[] array, int... indices) {
            return array[getIndex(indices)];
        }

        @Override
        public PrimitiveIterator.OfDouble iterator(double[] array) {
            return new ArrayStructureIterator(array);
        }

        @Override
        public DoubleStream stream(double[] array) {
            return Arrays.stream(array);
        }

        public class ArrayStructureIterator implements PrimitiveIterator.OfDouble {
            private final int[] indices = new int[dimension];
            private final double[] array;
            private int arrayIndex = offset;
            private int index = 0;

            public ArrayStructureIterator(double[] array) {
                this.array = array;
            }

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public double nextDouble() {
                double next = array[arrayIndex];
                index++;

                for (int i = dimension - 1; i >= 0; i--) {
                    if (++indices[i] < dimensions[i]) {
                        arrayIndex += strides[i];
                        return next;
                    }

                    indices[i] = 0;
                    arrayIndex -= ((dimensions[i] - 1) * strides[i]);
                }

                return next;
            }
        }

        public class ContiguousArrayStructureIterator implements PrimitiveIterator.OfDouble {
            private final int fence = offset + size;
            private final double[] array;
            private int arrayIndex = offset;

            public ContiguousArrayStructureIterator(double[] array) {
                this.array = array;
            }

            @Override
            public boolean hasNext() {
                return arrayIndex < fence;
            }

            @Override
            public double nextDouble() {
                return array[arrayIndex++];
            }
        }
    }

    @Override
    public PrimitiveIterator.OfDouble iterator() {
        return getStructure().iterator(array);
    }
}
