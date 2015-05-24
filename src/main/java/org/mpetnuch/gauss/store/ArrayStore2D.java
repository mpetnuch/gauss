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

        final ArrayStructure2D structure = new RowMajorArrayStructure2D(rowCount, columnCount);
        return new ArrayStore2D(structure.flatten(array), structure);
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

    public static class ArrayStore2DBuilder {
        private final int rowCount, columnCount;
        private int offset = 0;

        public ArrayStore2DBuilder(int rowCount, int columnCount) {
            this.rowCount = rowCount;
            this.columnCount = columnCount;
        }

        public ArrayStore2D build(double[] array, int stride, ArrayElementOrder arrayElementOrder) {
            return new ArrayStore2D(array, getStructure(arrayElementOrder, stride));
        }

        public ArrayStore2D build(double[] array, ArrayElementOrder arrayElementOrder) {
            if (arrayElementOrder == ArrayElementOrder.ColumnMajor) {
                return new ArrayStore2D(array, getStructure(arrayElementOrder, rowCount));
            } else if (arrayElementOrder == ArrayElementOrder.RowMajor) {
                return new ArrayStore2D(array, getStructure(arrayElementOrder, columnCount));
            } else {
                throw new AssertionError();
            }
        }

        private ArrayStructure2D getStructure(ArrayElementOrder arrayElementOrder, int stride) {
            if (arrayElementOrder == ArrayElementOrder.ColumnMajor) {
                if (rowCount <= stride) {
                    return new ColumnMajorArrayStructure2D(rowCount, columnCount, stride, offset);
                } else {
                    throw new IllegalArgumentException(String.format(
                            "ColumnMajor stride[%d] < rowCount[%d]!", stride, columnCount));
                }
            } else if (arrayElementOrder == ArrayElementOrder.RowMajor) {
                if (columnCount <= stride) {
                    return new RowMajorArrayStructure2D(rowCount, columnCount, stride, offset);
                } else {
                    throw new IllegalArgumentException(String.format(
                            "RowMajor stride[%d] < columnCount[%d]!", stride, columnCount));
                }
            } else {
                throw new AssertionError("Unknown ArrayElementOrder: " + arrayElementOrder);
            }
        }

        public ArrayStore2DBuilder setOffset(int offset) {
            this.offset = offset;
            return this;
        }
    }

    private static final class RowMajorArrayStructure2D extends ArrayStructure2D {
        private RowMajorArrayStructure2D(int rowCount, int columnCount, int stride, int offset) {
            super(ArrayElementOrder.RowMajor, rowCount, columnCount, stride, offset);
        }

        private RowMajorArrayStructure2D(int rowCount, int columnCount) {
            this(rowCount, columnCount, columnCount, 0);
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
        private ColumnMajorArrayStructure2D(int rowCount, int columnCount, int stride, int offset) {
            super(ArrayElementOrder.ColumnMajor, rowCount, columnCount, stride, offset);
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

        private ArrayStructure2D(ArrayElementOrder arrayElementOrder, int rowCount, int columnCount, int stride, int offset) {
            super(new int[]{rowCount, columnCount}, getStrideArray(arrayElementOrder, stride), offset);
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
