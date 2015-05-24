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
        return new ArrayStore1D(array, new ArrayStructure1D(0, array.length, 1));
    }

    @Override
    public double get(int index) {
        return structure.get(array, index);
    }

    @Override
    public ArrayStructure1D getStructure() {
        return structure;
    }

    public static class ArrayStructure1D extends ArrayStructure {
        final int stride;

        public ArrayStructure1D(int offset, int length, int stride) {
            super(offset, new int[]{length}, new int[]{stride});
            this.stride = stride;
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
