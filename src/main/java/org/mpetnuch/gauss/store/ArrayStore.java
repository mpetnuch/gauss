package org.mpetnuch.gauss.store;

import java.util.*;
import java.util.function.DoubleConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public abstract class ArrayStore implements StoreAnyD {
    protected final double[] array;

    protected ArrayStore(double[] array) {
        this.array = array;
    }

    abstract ArrayStructure getStructure();

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

    @Override
    public PrimitiveIterator.OfDouble iterator() {
        return getStructure().iterator(array);
    }

    protected abstract static class ArrayStructure implements Structure {
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

        abstract Spliterator.OfDouble spliterator(double[] array);

        private int getIndex(int... indices) {
            int index = offset;
            for (int n = 0; n < dimension; n++) {
                index += indices[n] * strides[n];
            }

            return index;
        }

        double get(double[] array, int... indices) {
            return array[getIndex(indices)];
        }

        PrimitiveIterator.OfDouble iterator(double[] array) {
            return Spliterators.iterator(spliterator(array));
        }

        DoubleStream stream(double[] array) {
            return StreamSupport.doubleStream(spliterator(array), false);
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

        public class ArrayStructureSpliterator implements Spliterator.OfDouble {
            private final int[] indices;
            private final double[] array;
            private final int fence;  // one past last element to be processed
            private final int characteristics = ORDERED | IMMUTABLE | SIZED | SUBSIZED;
            private int index;        // current index int the array, modified on advance/split
            private int elementIndex; // current element to be processed, modified on advance/split

            public ArrayStructureSpliterator(double[] array) {
                this.array = array;
                this.indices = new int[dimension];

                this.fence = size;
                this.index = offset;
                this.elementIndex = 0;
            }

            public ArrayStructureSpliterator(double[] array, int[] indices, int index, int elementIndex, int fence) {
                this.array = array;
                this.indices = indices;
                this.index = index;
                this.elementIndex = elementIndex;
                this.fence = fence;
            }

            @Override
            public OfDouble trySplit() {
                final int lo = index, loElement = elementIndex, midElement = (loElement + size) >>> 1;
                if (loElement >= midElement) {
                    return null;
                }

                // copy the current indicies as the lo indicies
                final int[] loIndicies = indices.clone();

                // find the number of elements we need to advance the indices by
                int splitAdvance = midElement - loElement;

                // find the number of elements advanced by increasing a dimension's index value
                final int[] subDimensionSize = new int[dimension];
                for (int i = dimension - 1, p = 1; i >= 0; i--) {
                    subDimensionSize[i] = p;
                    p *= dimensionLength(i);
                }

                // We need to find the index of max sub-dimension for which the requested element is in. Once we
                // have that we that we can adjust all the other sub dimensions to the exact requested position
                int maxSubDimension = dimension - 1;
                while (maxSubDimension >= 0) {
                    final int n = subDimensionSize[maxSubDimension];
                    final int l = dimensionLength(maxSubDimension);
                    for (int j = indices[maxSubDimension]; splitAdvance >= n && j < l; j++) {
                        indices[maxSubDimension]++;
                        splitAdvance -= n;
                    }

                    if (indices[maxSubDimension] == l) {
                        // We adjusted all we can in this dimension, so we zero out the index and advance the
                        // next dimension
                        indices[maxSubDimension] = 0;
                        indices[maxSubDimension - 1]++;
                    }

                    maxSubDimension--;
                }

                // go up in the dimensions and isolate the exact position
                for (int subDimension = maxSubDimension + 1; subDimension < dimension; subDimension++) {
                    final int n = subDimensionSize[subDimension];
                    final int l = dimensionLength(subDimension);
                    for (int j = indices[subDimension]; splitAdvance >= n && j < l; j++) {
                        indices[subDimension]++;
                        splitAdvance -= n;
                    }
                }

                // set index based on the newly adjusted indicies
                index = getIndex(indices);

                return new ArrayStructureSpliterator(array, loIndicies, lo, loElement, elementIndex = midElement);
            }

            @Override
            public void forEachRemaining(DoubleConsumer action) {
                Objects.requireNonNull(action);

                // hoist accesses and checks from loop
                final int hi;
                int i;
                if (array.length >= (hi = fence) && (i = index) >= 0 && i < (index = hi)) {
                    do {
                        action.accept(array[i]);
                    } while ((i = nextIndex(i)) < hi);
                }
            }

            @Override
            public boolean tryAdvance(DoubleConsumer action) {
                Objects.requireNonNull(action);

                if (elementIndex > 0 && elementIndex < fence) {
                    action.accept(array[index]);
                    index = nextIndex(index);
                    elementIndex++;
                    return true;
                }

                return false;
            }

            private int nextIndex(int currentIndex) {
                for (int i = dimension - 1; i >= 0; i--) {
                    if (++indices[i] < dimensions[i]) {
                        currentIndex += strides[i];
                        return currentIndex;
                    }

                    indices[i] = 0;
                    currentIndex -= ((dimensions[i] - 1) * strides[i]);
                }

                return currentIndex;
            }

            @Override
            public long estimateSize() {
                return (long) (size - elementIndex);
            }

            @Override
            public int characteristics() {
                return characteristics;
            }
        }

        public class StridedArrayStructureSpliterator implements Spliterator.OfDouble {
            private final double[] array;
            private final int stride;
            private final int fence;  // one past last index
            private final int characteristics = IMMUTABLE | ORDERED | SIZED | SUBSIZED;
            private int index;        // current index, modified on advance/split

            public StridedArrayStructureSpliterator(double[] array, int stride) {
                this.array = array;
                this.stride = stride;

                this.index = offset;
                this.fence = offset + size * stride;
            }

            public StridedArrayStructureSpliterator(double[] array, int stride, int index, int fence) {
                this.array = array;
                this.stride = stride;

                this.index = index;
                this.fence = fence;
            }

            @Override
            public OfDouble trySplit() {
                // mid is the current index adjusted by the half the remaining elements
                // times the stride width
                final int lo = index, mid = lo + (((fence - lo) / stride) >>> 1) * stride;
                if (lo >= mid) {
                    return null;
                }

                return new StridedArrayStructureSpliterator(array, stride, lo, index = mid);
            }

            @Override
            public void forEachRemaining(DoubleConsumer action) {
                Objects.requireNonNull(action);

                // hoist accesses and checks from loop
                final int hi;
                int i;
                if (array.length >= (hi = fence) && (i = index) >= 0 && i < (index = hi)) {
                    do {
                        action.accept(array[i]);
                    } while ((i = nextIndex(i)) < hi);
                }
            }

            @Override
            public boolean tryAdvance(DoubleConsumer action) {
                Objects.requireNonNull(action);

                if (index > 0 && index < fence) {
                    action.accept(array[index]);
                    index = nextIndex(index);
                    return true;
                }

                return false;
            }

            private int nextIndex(int currentIndex) {
                return currentIndex + stride;
            }

            @Override
            public long estimateSize() {
                return (long) (size - (fence - index) * stride);
            }

            @Override
            public int characteristics() {
                return characteristics;
            }
        }
    }
}
