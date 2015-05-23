package org.mpetnuch.gauss.matrix.accessor;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public final class ArrayElementAccessor2D extends AbstractArrayElementAccessor2D<ArrayElementAccessor2D> {
    public ArrayElementAccessor2D(double[] elements, ArrayElementOrder elementOrder, int offset, int stride, int rowCount, int columnCount) {
        super(elements, elementOrder, offset, stride, rowCount, columnCount);
    }

    @Override
    ArrayElementAccessor2D create(double[] elements, ArrayElementOrder elementOrder, int offset, int stride, int rowCount, int columnCount) {
        return new ArrayElementAccessor2D(elements, elementOrder, offset, stride, rowCount, columnCount);
    }
}
