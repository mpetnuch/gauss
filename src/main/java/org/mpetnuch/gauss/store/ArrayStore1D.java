package org.mpetnuch.gauss.store;

import java.util.Arrays;
import java.util.Spliterator;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class ArrayStore1D extends ArrayStore implements Store1D {
    private final ArrayStructure1D structure;

    private ArrayStore1D(double[] array, ArrayStructure1D structure) {
        super(array);

        this.structure = structure;
    }

    public ArrayStore1D from(double[] array) {
        return new ArrayStore1D(array, new ArrayStructure1D(array.length, 1, 0));
    }

    @Override
    public double get(int index) {
        return structure.get(array, index);
    }

    @Override
    public ArrayStructure1D getStructure() {
        return structure;
    }

    public ArrayStore1D slice(int startIndex, int endIndex) {
        return null;
    }

    @Override
    public ArrayStore2D reshape(int rowCount, int columnCount) {
        return null;
    }

    @Override
    public ArrayStore1D compact() {
        if (structure.offset == 0 && structure.stride == 1) {
            return this;
        }

        return this;
    }

    @Override
    public void copyInto(double[] copy, int offset) {
        if (structure.stride == 1) {
            System.arraycopy(array, structure.offset, copy, offset, structure.size);
            return;
        }

        final int fence = structure.offset + structure.size * structure.stride;
        // else we need to access the array in a strided fashion to copy into the requested array
        for (int i = structure.offset, k = offset; i < fence; i += structure.stride) {
            copy[k++] = array[i];
        }
    }

    public static class ArrayStore1DBuilder {
        private final int length;
        private int offset = 0;
        private int stride = 1;

        public ArrayStore1DBuilder(int length) {
            this.length = length;
        }

        public ArrayStore1D build(double[] array) {
            return new ArrayStore1D(array, new ArrayStructure1D(length, stride, offset));
        }

        public ArrayStore1DBuilder setStride(int stride) {
            this.stride = stride;
            return this;
        }

        public ArrayStore1DBuilder setOffset(int offset) {
            this.offset = offset;
            return this;
        }
    }

    public static class ArrayStructure1D extends ArrayStructure {
        final int stride;

        public ArrayStructure1D(int length, int stride, int offset) {
            super(new int[]{length}, new int[]{stride}, offset);
            this.stride = stride;
        }

        public ArrayStructure1D(int length) {
            this(length, 1, 0);
        }

        @Override
        Spliterator.OfDouble spliterator(double[] array) {
            if (stride == 1) {
                return Arrays.spliterator(array, offset, offset + size);
            } else {
                return new StridedArrayStructureSpliterator(array, stride);
            }
        }

        public double get(double[] array, int index) {
            return array[offset + index * stride];
        }

        @Override
        public int dimension() {
            return 1;
        }
    }
}
