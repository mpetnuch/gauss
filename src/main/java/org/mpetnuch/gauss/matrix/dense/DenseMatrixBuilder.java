package org.mpetnuch.gauss.matrix.dense;

import org.mpetnuch.gauss.matrix.MatrixBuilder;
import org.mpetnuch.gauss.matrix.accessor.MutableArrayElementAccessor2D;

import static org.mpetnuch.gauss.matrix.accessor.ArrayElementOrder.RowMajor;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class DenseMatrixBuilder implements MatrixBuilder<DenseMatrix, DenseMatrixBuilder> {
    private final MutableArrayElementAccessor2D elementAccessor;

    private DenseMatrixBuilder(MutableArrayElementAccessor2D elementAccessor) {
        this.elementAccessor = elementAccessor;
    }

    public double[] unsafeGetElements() {
        return elementAccessor.elements;
    }

    public static DenseMatrixBuilder create(int rowCount, int columnCount) {
        final double[] elements = new double[rowCount * columnCount];
        MutableArrayElementAccessor2D elementAccessor = new MutableArrayElementAccessor2D(elements, RowMajor, 0, columnCount, rowCount, columnCount);
        return new DenseMatrixBuilder(elementAccessor);
    }

    @Override
    public DenseMatrixBuilder scale(double alpha) {
        if (Double.compare(alpha, 0.0) == 0) {
            return this;
        } else if (Double.compare(1.0, alpha) == 0) {
            return this;
        }

        elementAccessor.replaceAll(x -> x * alpha);
        return this;
    }

    @Override
    public DenseMatrixBuilder add(int rowIndex, int columnIndex, double alpha) {
        elementAccessor.increment(rowIndex, columnIndex, alpha);
        return this;
    }

    public DenseMatrixBuilder set(int rowIndex, int columnIndex, double alpha) {
        elementAccessor.set(rowIndex, columnIndex, alpha);
        return this;
    }

    @Override
    public DenseMatrixBuilder slice(int rowIndexStart, int rowIndexEnd, int columnIndexStart, int columnIndexEnd) {
        return new DenseMatrixBuilder(elementAccessor.slice(rowIndexStart, rowIndexEnd, columnIndexStart, columnIndexEnd));
    }

    @Override
    public DenseMatrix build() {
        return new DenseGeneralMatrix(elementAccessor.immutableCopy());
    }
}
