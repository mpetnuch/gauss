package org.mpetnuch.gauss.matrix.accessor;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public final class MutableArrayElementAccessor2D extends AbstractArrayElementAccessor2D<MutableArrayElementAccessor2D> {
    public MutableArrayElementAccessor2D(double[] elements, ArrayElementOrder elementOrder, int offset, int stride, int rowCount, int columnCount) {
        super(elements, elementOrder, offset, stride, rowCount, columnCount);
    }

    @Override
    MutableArrayElementAccessor2D create(double[] elements, ArrayElementOrder elementOrder, int offset, int stride, int rowCount, int columnCount) {
        return new MutableArrayElementAccessor2D(elements, elementOrder, offset, stride, rowCount, columnCount);
    }

    public void set(int rowIndex, int columnIndex, double value) {
        elements[getIndex(rowIndex, columnIndex)] = value;
    }

    public void increment(int rowIndex, int columnIndex, double value) {
        elements[getIndex(rowIndex, columnIndex)] += value;
    }

    public void replaceAll(DoubleUnaryOperator operator) {
        Arrays.setAll(elements, operator::applyAsDouble);
    }

    public ArrayElementAccessor2D immutableCopy() {
        return new ArrayElementAccessor2D(elements.clone(), elementOrder, offset, stride, rowCount, columnCount);
    }
}
