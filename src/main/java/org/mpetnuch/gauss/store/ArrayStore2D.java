package org.mpetnuch.gauss.store;

import org.mpetnuch.gauss.matrix.accessor.ArrayElementOrder;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * @author Michael Petnuch
 * @id $Id$
 */
final class ArrayStore2D extends ArrayStore implements Store2D {
    final ArrayStructure2D structure;

    protected ArrayStore2D(double[] array, ArrayStructure2D structure)



    {
        super(array);
        this.structure = structure;


    }









    @Override
    public double get(int rowIndex, int columnIndex) {
        return structure.get(array, rowIndex, columnIndex);
    }

    @Override
    public ArrayStructure2D getStructure() {
        return structure;
    }

    static final class RowMajorArrayStructure2D extends ArrayStructure2D {

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

    static final class ColumnMajorArrayStructure2D extends ArrayStructure2D {
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

    public static abstract class ArrayStructure2D extends ArrayStructure {
        final ArrayElementOrder arrayElementOrder;
        final int stride;

        public abstract double[] flatten(double[][] array2D);

        public abstract int index(int rowIndex, int columnIndex);

        private ArrayStructure2D(ArrayElementOrder arrayElementOrder, int offset, int rowCount, int columnCount, int stride) {
            super(offset, new int[]{rowCount, columnCount}, getStrideArray(arrayElementOrder, stride));
            this.arrayElementOrder = arrayElementOrder;
            this.stride = stride;
        }

        private static int[] getStrideArray(ArrayElementOrder arrayElementOrder, int stride) {
            return ArrayElementOrder.RowMajor == arrayElementOrder ?
                    new int[]{1, stride} :
                    new int[]{stride, 1};
        }

        public int getRowCount() {
            return dimensions[0];
        }

        public int getColumnCount() {
            return dimensions[1];
        }

        public <T> T get(T[] array, int rowIndex, int columnIndex) {
            return array[index(rowIndex, columnIndex)];
        }

        @Override
        public int dimension() {
            return 2;
        }

        @Override
        public DoubleStream stream(double[] array) {
            int strideDimension = arrayElementOrder.getStrideDimension();
            if(this.stride == this.dimensionLength(strideDimension)) {
                Arrays.stream(array, offset, size);
            }

            return super.stream(array);
        }

        @Override
        public PrimitiveIterator.OfDouble iterator(double[] array) {
            int strideDimension = arrayElementOrder.getStrideDimension();
            if(this.stride == this.dimensionLength(strideDimension)) {
                return new ContiguousArrayStructureIterator(array);
            }

            return super.iterator(array);
        }
    }
}
