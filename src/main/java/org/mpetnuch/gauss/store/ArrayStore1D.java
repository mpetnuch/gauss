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
