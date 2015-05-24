package org.mpetnuch.gauss.store;

import org.mpetnuch.gauss.matrix.accessor.ArrayElementOrder;

import java.util.Arrays;
import java.util.Spliterator;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
final class ArrayStore2D extends ArrayStore implements Store2D {
    final ArrayStructure2D structure;

    protected ArrayStore2D(double[] array, ArrayStructure2D structure) {
        super(array);
        this.structure = structure;
    }

    public static ArrayStore2D from(double[][] array) {
        final int rowCount = array.length;
        final int columnCount = array[0].length;

        final ArrayStructure2D structure =
                new ArrayStore2D.ColumnMajorArrayStructure2D(0, rowCount, columnCount, rowCount);

        return new ArrayStore2D(structure.flatten(array), structure);
    }

    public static ArrayStore2D from(double[] array, int rowCount, int columnCount, ArrayElementOrder arrayElementOrder) {
        ArrayStructure2D structure;
        switch (arrayElementOrder) {
            case ColumnMajor:
                structure = new ArrayStore2D.ColumnMajorArrayStructure2D(0, rowCount, columnCount, rowCount);
                break;
            case RowMajor:
                structure = new ArrayStore2D.RowMajorArrayStructure2D(0, rowCount, columnCount, rowCount);
                break;
            default:
                throw new AssertionError();
        }

        return new ArrayStore2D(array, structure);
    }

    @Override
    ArrayStructure2D getStructure() {
        return structure;
    }

    @Override
    public double get(int... indices) {
        if (getStructure().dimension() != indices.length) {
            throw new IllegalArgumentException();
        }

        return get(indices[0], indices[1]);
    }

    @Override
    public double get(int rowIndex, int columnIndex) {
        return structure.get(array, rowIndex, columnIndex);
    }

    private static final class RowMajorArrayStructure2D extends ArrayStructure2D {
        public RowMajorArrayStructure2D(int offset, int rowCount, int columnCount, int stride) {
            super(ArrayElementOrder.RowMajor, offset, rowCount, columnCount, stride);
        }

        @Override
        public final int index(int rowIndex, int columnIndex) {
            return offset + rowIndex * stride + columnIndex;
        }

        @Override
        public  double[] flatten(double[][] array2D) {
            final int rowCount = getRowCount();
            final int columnCount = getColumnCount();

            double[] array = new double[size()];
            for(int index = 0, rowIndex = 0; rowIndex < rowCount; rowIndex++, index += columnCount) {
                System.arraycopy(array2D[rowIndex], 0, array, index, columnCount);
            }

            return array;
        }
    }

    private static final class ColumnMajorArrayStructure2D extends ArrayStructure2D {
        public ColumnMajorArrayStructure2D(int offset, int rowCount, int columnCount, int stride) {
            super(ArrayElementOrder.ColumnMajor, offset, rowCount, columnCount, stride);
        }

        @Override
        public final int index(int rowIndex, int columnIndex) {
            return offset + columnIndex * stride + rowIndex;
        }

        @Override
        public double[] flatten(double[][] array2D) {
            final int rowCount = getRowCount();
            final int columnCount = getColumnCount();

            double[] array = new double[size()];
            for(int index = 0, rowIndex = 0; rowIndex < rowCount; rowIndex++, index += columnCount) {
                for(int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                    array[index(rowIndex, columnIndex)] = array2D[rowIndex][columnIndex];
                }
            }

            return array;
        }
    }

    private static abstract class ArrayStructure2D extends ArrayStructure {
        final ArrayElementOrder arrayElementOrder;
        final int stride;

        private ArrayStructure2D(ArrayElementOrder arrayElementOrder, int offset, int rowCount, int columnCount, int stride) {
            super(offset, new int[]{rowCount, columnCount}, getStrideArray(arrayElementOrder, stride));
            this.arrayElementOrder = arrayElementOrder;
            this.stride = stride;
        }

        private static int[] getStrideArray(ArrayElementOrder arrayElementOrder, int stride) {
            switch (arrayElementOrder) {
                case ColumnMajor:
                    return new int[]{stride, 1};
                case RowMajor:
                    return new int[]{1, stride};
                default:
                    throw new AssertionError();
            }
        }

        abstract double[] flatten(double[][] array2D);

        abstract int index(int rowIndex, int columnIndex);

        @Override
        Spliterator.OfDouble spliterator(double[] array) {
            final int strideDimension = arrayElementOrder.getStrideDimension();
            if (stride == dimensionLength(strideDimension)) {
                return Arrays.spliterator(array, offset, size);
            } else {
                return new StridedArrayStructureSpliterator(array, stride);
            }
        }

        @Override
        public int dimension() {
            return 2;
        }

        public int getRowCount() {
            return dimensions[0];
        }

        public int getColumnCount() {
            return dimensions[1];
        }

        public double get(double[] array, int rowIndex, int columnIndex) {
            return array[index(rowIndex, columnIndex)];
        }
    }
}
