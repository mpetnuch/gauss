package org.mpetnuch.gauss.matrix.dense;

import org.mpetnuch.gauss.matrix.MatrixDiagonalType;
import org.mpetnuch.gauss.matrix.MatrixSide;
import org.mpetnuch.gauss.matrix.TriangularMatrix;
import org.mpetnuch.gauss.matrix.TriangularMatrixType;
import org.mpetnuch.gauss.matrix.accessor.ArrayElementAccessor2D;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class DenseTriangularMatrix extends DenseMatrix implements TriangularMatrix {
    private final TriangularMatrixType triangularMatrixType;
    private final MatrixDiagonalType matrixDiagonalType;

    DenseTriangularMatrix(ArrayElementAccessor2D elementAccessor, TriangularMatrixType triangularMatrixType, MatrixDiagonalType matrixDiagonalType) {
        super(elementAccessor);
        this.triangularMatrixType = triangularMatrixType;
        this.matrixDiagonalType = matrixDiagonalType;
    }

    public DenseMatrix multiply(DenseMatrix that) {
        final int M = this.getNumberOfRows(), N = that.getNumberOfColumns();
        DenseMatrixBuilder resultBuilder = DenseMatrixBuilder.create(M, N);
        blasLevel3.dtrmm(1.0, MatrixSide.LEFT, this, that, 0.0, resultBuilder);
        return resultBuilder.build();
    }

    @Override
    public DenseTriangularMatrix triangularSlice(int rowStart, int rowEnd) {
        return create(elementAccessor.slice(rowStart, rowEnd, rowStart, rowEnd));
    }

    @Override
    DenseTriangularMatrix create(ArrayElementAccessor2D elementAccessor) {
        return new DenseTriangularMatrix(elementAccessor, triangularMatrixType, matrixDiagonalType);
    }

    @Override
    public TriangularMatrixType getTriangularMatrixType() {
        return triangularMatrixType;
    }

    @Override
    public MatrixDiagonalType getMatrixDiagonalType() {
        return matrixDiagonalType;
    }
}
