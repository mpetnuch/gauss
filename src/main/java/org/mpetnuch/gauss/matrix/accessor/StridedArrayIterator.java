package org.mpetnuch.gauss.matrix.accessor;

import java.util.PrimitiveIterator;


/**
* @author Michael Petnuch
* @version $Id$
*/
public final class StridedArrayIterator implements PrimitiveIterator.OfDouble {
    private double[] elements;
    private final int stride, fence;
    private int k;

    public StridedArrayIterator(double[] elements, int offset, int stride, int length) {
        this.elements = elements;
        this.fence = offset + length * stride;
        this.k = offset;
        this.stride = stride;
    }

    @Override
    public double nextDouble() {
        double next = elements[k];
        k += stride;
        return next;
    }

    @Override
    public boolean hasNext() {
        return k < fence;
    }
}
