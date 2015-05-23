package org.mpetnuch.gauss.matrix.accessor;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public final class ArrayElementAccessor1D {
    private final double[] elements;
    private final int offset, stride, length;

    public ArrayElementAccessor1D(double[] elements, int offset, int stride, int length) {
        this.elements = elements;
        this.offset = offset;
        this.stride = stride;
        this.length = length;
    }

    public final double get(int index) {
        return elements[offset + stride * index];
    }

    public int getLength() {
        return length;
    }

    public ArrayElementAccessor1D slice(int startIndex, int endIndex) {
        return new ArrayElementAccessor1D(elements, offset + startIndex, stride, endIndex - startIndex);
    }

    public ArrayElementAccessor2D reshape(int rowCount, int columnCount) {
        if (stride == 1) {
            return new ArrayElementAccessor2D(elements, ArrayElementOrder.RowMajor, offset, 1, rowCount, columnCount);
        } else if (rowCount == 1 || columnCount == 1) {
            return new ArrayElementAccessor2D(elements, ArrayElementOrder.RowMajor, offset, stride, rowCount, columnCount);
        }

        return new ArrayElementAccessor2D(toArray(), ArrayElementOrder.RowMajor, 0, columnCount, rowCount, columnCount);
    }

    public DoubleStream stream() {
        if (stride == 1) {
            return Arrays.stream(elements, offset, offset + length);
        }

        StridedArrayIterator iterator = new StridedArrayIterator(elements, offset, stride, length);
        final int characteristics = Spliterator.ORDERED | Spliterator.IMMUTABLE;
        Spliterator.OfDouble spliterators = Spliterators.spliterator(iterator, length, characteristics);
        return StreamSupport.doubleStream(spliterators, false);
    }

    public double[] toArray() {
        double[] copy = new double[length];
        toArray(copy);
        return copy;
    }

    public void toArray(double[] array) {
        if (stride == 1) {
            System.arraycopy(elements, offset, array, 0, length);
            return;
        }

        Arrays.setAll(array, this::get);
    }

    public ArrayElementAccessor1D compact() {
        if (offset == 0 && stride == 1) {
            return this;
        } else {
            return new ArrayElementAccessor1D(toArray(), 0, 1, length);
        }
    }
}
