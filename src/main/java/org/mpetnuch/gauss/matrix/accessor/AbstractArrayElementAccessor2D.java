package org.mpetnuch.gauss.matrix.accessor;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public abstract class AbstractArrayElementAccessor2D<T extends AbstractArrayElementAccessor2D<T>> {
    public final double[] elements;
    final int offset, stride;
    final int rowCount, columnCount;
    final ArrayElementOrder elementOrder;

    abstract T create(double[] elements, ArrayElementOrder elementOrder, int offset, int stride, int rowCount, int columnCount);

    protected AbstractArrayElementAccessor2D(double[] elements, ArrayElementOrder elementOrder, int offset, int stride, int rowCount, int columnCount) {
        this.elements = elements;
        this.offset = offset;
        this.stride = stride;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.elementOrder = elementOrder;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    final int getIndex(int rowIndex, int columnIndex) {
        if (ArrayElementOrder.RowMajor == elementOrder) {
            return offset + rowIndex * stride + columnIndex;
        } else {
            return offset + columnIndex * stride + rowIndex;
        }
    }

    public double get(int rowIndex, int columnIndex) {
        return elements[getIndex(rowIndex, columnIndex)];
    }

    public ArrayElementAccessor1D getColumn(int columnIndex) {
        if (ArrayElementOrder.RowMajor == elementOrder) {
            return new ArrayElementAccessor1D(elements, offset + columnIndex, stride, rowCount);
        } else {
            return new ArrayElementAccessor1D(elements, offset + stride * columnIndex, 1, rowCount);
        }
    }

    public ArrayElementAccessor1D getRow(int rowIndex) {
        if (ArrayElementOrder.RowMajor == elementOrder) {
            return new ArrayElementAccessor1D(elements, offset + stride * rowIndex, 1, columnCount);
        } else {
            return new ArrayElementAccessor1D(elements, offset + rowIndex, stride, columnCount);
        }
    }

    public T slice(int rowIndexStart, int rowIndexEnd, int columnIndexStart, int columnIndexEnd) {
        final int rows = rowIndexEnd - rowIndexStart, columns = columnIndexEnd - columnIndexStart;
        final int offset;
        if (ArrayElementOrder.RowMajor == elementOrder) {
            offset = this.offset + stride * rowIndexStart + columnIndexStart;
        } else {
            offset = this.offset + stride * columnIndexStart + rowIndexStart;
        }

        return create(elements, elementOrder, offset, stride, rows, columns);
    }

    public T reshape(int rowCount, int columnCount) {
        int padding = stride - (ArrayElementOrder.RowMajor == elementOrder ? this.columnCount : this.rowCount);
        if (padding == 0) { // array is contiguous so we can directly reshape
            return create(elements, elementOrder, offset, stride, rowCount, columnCount);
        }

        return null;
    }

    public T transpose() {
        if (ArrayElementOrder.RowMajor == elementOrder) {
            return create(elements, ArrayElementOrder.ColumnMajor, offset, stride, columnCount, rowCount);
        } else {
            return create(elements, ArrayElementOrder.RowMajor, offset, stride, columnCount, rowCount);
        }
    }

    public T compact() {
        if (offset == 0 && stride == 1) {
            return create(elements, elementOrder, offset, stride, rowCount, columnCount);
        } else if (ArrayElementOrder.RowMajor == elementOrder) {
            double[] elements = new double[rowCount * columnCount];
            for (int rowIndex = 0, k = 0; rowIndex < rowCount; rowIndex++) {
                for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                    elements[k++] = get(rowIndex, columnIndex);
                }
            }

            return create(elements, ArrayElementOrder.RowMajor, 0, columnCount, rowCount, columnCount);
        } else {
            double[] elements = new double[rowCount * columnCount];
            for (int columnIndex = 0, k = 0 ; columnIndex < columnCount; columnIndex++) {
                for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                    elements[k++] = get(rowIndex, columnIndex);
                }
            }

            return create(elements, ArrayElementOrder.ColumnMajor, 0, rowCount, rowCount, columnCount);
        }
    }

    public double[] toArray(ArrayElementOrder elementOrder) {
        double[] copy = new double[rowCount * columnCount];
        toArray(copy, elementOrder);
        return copy;
    }

    public void toArray(double[] copy, ArrayElementOrder elementOrder) {
        if (ArrayElementOrder.RowMajor == elementOrder) {
            for (int rowIndex = 0, k = 0; rowIndex < rowCount; rowIndex++) {
                for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                    copy[k++] = get(rowIndex, columnIndex);
                }
            }
        } else {
            for (int columnIndex = 0, k = 0 ; columnIndex < columnCount; columnIndex++) {
                for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                    copy[k++] = get(rowIndex, columnIndex);
                }
            }
        }
    }
}
