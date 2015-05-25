package org.mpetnuch.gauss.store;

import java.util.Arrays;
import java.util.Spliterator;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class ArrayStore2D extends ArrayStore implements Store2D {
    final ArrayStructure2D structure;

    protected ArrayStore2D(double[] array, ArrayStructure2D structure) {
        super(array);
        this.structure = structure;
    }

    public static ArrayStore2D from(double[][] array) {
        final int rowCount = array.length;
        final int columnCount = array[0].length;

        return new ArrayStore2DBuilder(rowCount, columnCount).
                setStride(columnCount).
                build(flatten(array), ArrayElementOrder.RowMajor);
    }

    private static double[] flatten(double[][] array) {
        final int rowCount = array.length;
        final int columnCount = array[0].length;

        double[] flattenedArray = new double[rowCount * columnCount];
        for (int index = 0, rowIndex = 0; rowIndex < rowCount; rowIndex++, index += columnCount) {
            System.arraycopy(array[rowIndex], 0, flattenedArray, index, columnCount);
        }

        return flattenedArray;
    }

    @Override
    public ArrayStructure2D getStructure() {
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

    @Override
    public ArrayStore1D getColumn(int columnIndex) {
        ArrayStore1D.ArrayStore1DBuilder builder = new ArrayStore1D.ArrayStore1DBuilder(structure.getRowCount());
        if (ArrayElementOrder.RowMajor == structure.arrayElementOrder) {
            builder.setOffset(structure.offset + columnIndex);
            builder.setStride(structure.stride);
        } else {
            builder.setOffset(structure.offset + structure.stride * columnIndex);
            builder.setStride(1);
        }

        return builder.build(array);
    }

    @Override
    public ArrayStore1D getRow(int rowIndex) {
        ArrayStore1D.ArrayStore1DBuilder builder = new ArrayStore1D.ArrayStore1DBuilder(structure.getColumnCount());
        if (ArrayElementOrder.RowMajor == structure.arrayElementOrder) {
            builder.setOffset(structure.offset + structure.stride * rowIndex);
            builder.setStride(1);
        } else {
            builder.setOffset(structure.offset + rowIndex);
            builder.setStride(structure.stride);
        }

        return builder.build(array);
    }

    @Override
    public ArrayStore2D slice(int rowIndexStart, int rowIndexEnd, int columnIndexStart, int columnIndexEnd) {
        final int rows = rowIndexEnd - rowIndexStart, columns = columnIndexEnd - columnIndexStart;

        ArrayStore2DBuilder builder = new ArrayStore2DBuilder(rows, columns);
        builder.setStride(structure.stride);

        if (ArrayElementOrder.RowMajor == structure.arrayElementOrder) {
            builder.setOffset(structure.offset + structure.stride * rowIndexStart + columnIndexStart);
        } else {
            builder.setOffset(structure.offset + structure.stride * columnIndexStart + rowIndexStart);
        }

        return builder.build(array, structure.arrayElementOrder);
    }

    @Override
    public ArrayStore2D transpose() {
        ArrayStore2DBuilder builder = new ArrayStore2DBuilder(structure.getColumnCount(), structure.getRowCount());
        builder.setOffset(structure.offset);
        builder.setStride(structure.stride);

        if (ArrayElementOrder.RowMajor == structure.arrayElementOrder) {
            return builder.build(array, ArrayElementOrder.ColumnMajor);
        } else {
            return builder.build(array, ArrayElementOrder.RowMajor);
        }
    }

    @Override
    public ArrayStore2D compact() {
        final int rowCount = structure.getRowCount();
        final int columnCount = structure.getColumnCount();

        return new ArrayStore2DBuilder(rowCount, columnCount).
                setStride(columnCount).
                build(toArray(), structure.arrayElementOrder);
    }

    public void copyInto(double[] copy, int offset) {
        final int rowCount = structure.getRowCount();
        final int columnCount = structure.getColumnCount();

        if (ArrayElementOrder.RowMajor == structure.arrayElementOrder) {
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                final ArrayStore1D row = getRow(rowIndex);
                row.copyInto(copy, offset + rowIndex * columnCount);
            }
        } else {
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                final ArrayStore1D column = getColumn(columnIndex);
                column.copyInto(copy, offset + columnIndex * rowCount);
            }
        }
    }

    public static class ArrayStore2DBuilder {
        protected final int rowCount, columnCount;
        protected int offset = 0;
        protected int stride = 1;

        public ArrayStore2DBuilder(int rowCount, int columnCount) {
            this.rowCount = rowCount;
            this.columnCount = columnCount;
        }

        public ArrayStore2D build(double[] array, ArrayElementOrder arrayElementOrder) {
            return new ArrayStore2D(array, getStructure(arrayElementOrder, stride));
        }

        protected ArrayStructure2D getStructure(ArrayElementOrder arrayElementOrder, int stride) {
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

        public ArrayStore2DBuilder setStride(int stride) {
            this.stride = stride;
            return this;
        }

        public ArrayStore2DBuilder setOffset(int offset) {
            this.offset = offset;
            return this;
        }
    }

    protected static final class RowMajorArrayStructure2D extends ArrayStructure2D {
        protected RowMajorArrayStructure2D(int rowCount, int columnCount, int stride, int offset) {
            super(ArrayElementOrder.RowMajor, rowCount, columnCount, stride, offset);
        }

        protected RowMajorArrayStructure2D(int rowCount, int columnCount) {
            this(rowCount, columnCount, columnCount, 0);
        }

        @Override
        public final int index(int rowIndex, int columnIndex) {
            return offset + rowIndex * stride + columnIndex;
        }
    }

    protected static final class ColumnMajorArrayStructure2D extends ArrayStructure2D {
        protected ColumnMajorArrayStructure2D(int rowCount, int columnCount, int stride, int offset) {
            super(ArrayElementOrder.ColumnMajor, rowCount, columnCount, stride, offset);
        }

        protected ColumnMajorArrayStructure2D(int rowCount, int columnCount) {
            this(rowCount, columnCount, rowCount, 0);
        }

        @Override
        public final int index(int rowIndex, int columnIndex) {
            return offset + columnIndex * stride + rowIndex;
        }
    }

    public static abstract class ArrayStructure2D extends ArrayStructure {
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

        private double get(double[] array, int rowIndex, int columnIndex) {
            return array[index(rowIndex, columnIndex)];
        }
    }
}
