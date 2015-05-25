package org.mpetnuch.gauss.matrix.dense;

import org.mpetnuch.gauss.matrix.MatrixBuilder;
import org.mpetnuch.gauss.store.MutableArrayStore2D;


/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class DenseMatrixBuilder implements MatrixBuilder<DenseMatrix, DenseMatrixBuilder> {
    private final MutableArrayStore2D arrayStore;

    private DenseMatrixBuilder(MutableArrayStore2D arrayStore) {
        this.arrayStore = arrayStore;
    }

    public static DenseMatrixBuilder create(int rowCount, int columnCount) {
        return new DenseMatrixBuilder(MutableArrayStore2D.of(rowCount, columnCount));
    }

    @Override
    public DenseMatrixBuilder scale(double alpha) {
        if (Double.compare(alpha, 0.0) == 0) {
            return this;
        } else if (Double.compare(1.0, alpha) == 0) {
            return this;
        }

        arrayStore.replaceAll(x -> x * alpha);
        return this;
    }

    @Override
    public DenseMatrixBuilder add(int rowIndex, int columnIndex, double alpha) {
        arrayStore.increment(rowIndex, columnIndex, alpha);
        return this;
    }

    public DenseMatrixBuilder set(int rowIndex, int columnIndex, double alpha) {
        arrayStore.set(rowIndex, columnIndex, alpha);
        return this;
    }

    @Override
    public DenseMatrixBuilder slice(int rowIndexStart, int rowIndexEnd, int columnIndexStart, int columnIndexEnd) {
        return new DenseMatrixBuilder(arrayStore.slice(rowIndexStart, rowIndexEnd, columnIndexStart, columnIndexEnd));
    }

    @Override
    public DenseMatrix build() {
        return new DenseGeneralMatrix(arrayStore.immutableCopy());
    }
}
