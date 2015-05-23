/*
 * Copyright (c) 2014, Michael Petnuch. All Rights Reserved.
 *
 * This file `DenseRealMatrix.java` is part of Gauss.
 *
 * Gauss is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mpetnuch.gauss.matrix.dense;

import java.util.stream.IntStream;

import org.mpetnuch.gauss.linearalgebra.blas3.BLASLevel3;
import org.mpetnuch.gauss.linearalgebra.blas3.JBLASLevel3;
import org.mpetnuch.gauss.matrix.Matrix;
import org.mpetnuch.gauss.matrix.MatrixDiagonalType;
import org.mpetnuch.gauss.matrix.MatrixSide;
import org.mpetnuch.gauss.matrix.MatrixType;
import org.mpetnuch.gauss.matrix.TriangularMatrixType;
import org.mpetnuch.gauss.matrix.accessor.ArrayElementAccessor1D;
import org.mpetnuch.gauss.matrix.accessor.ArrayElementAccessor2D;
import org.mpetnuch.gauss.matrix.accessor.ArrayElementOrder;

/**
 * @author Michael Petnuch
 * @id $Id$
 */
public abstract class DenseMatrix implements Matrix {
    private static final long serialVersionUID = -7150013139589271348L;
    private static final JBLASLevel3 DEFAULT_BLAS_LEVEL3 = new JBLASLevel3.JBLASLevel3Builder().createJBLASLevel3();

    protected final ArrayElementAccessor2D elementAccessor;
    protected BLASLevel3<DenseMatrix, DenseTriangularMatrix, DenseSymmetricMatrix, DenseMatrixBuilder> blasLevel3 = DEFAULT_BLAS_LEVEL3;

    DenseMatrix(ArrayElementAccessor2D elementAccessor) {
        this.elementAccessor = elementAccessor;
    }

    abstract DenseMatrix create(ArrayElementAccessor2D elementAccessor);

    public double[] unsafeGetElements() {
        return elementAccessor.elements;
    }

    public static DenseMatrix from(double[][] matrix) {
        int rowCount = matrix.length;
        int columnCount = matrix[0].length;
        for (int i = 1; i < rowCount; i++) {
            if (matrix[i].length != columnCount) {
                throw new IllegalArgumentException("Not a matrix, all rows must be same size.");
            }
        }

        double[] elements = new double[rowCount * columnCount];
        ArrayElementAccessor2D elementAccessor = new ArrayElementAccessor2D(elements, ArrayElementOrder.RowMajor, 0, columnCount, rowCount, columnCount);

        if (rowCount != columnCount) {
            IntStream.range(0, rowCount).forEach(rowIndex ->
                    System.arraycopy(matrix[rowIndex], 0, elements, rowIndex * columnCount, columnCount));

            return new DenseGeneralMatrix(elementAccessor);
        }

        // Special method for square-matrices, try to guess the matrix qualifiers
        boolean diagonal = true, upper = true, lower = true, symmetrical = true, unit = true;
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            System.arraycopy(matrix[rowIndex], 0, elements, rowIndex * columnCount, columnCount);

            for (int j = 0; j <= rowIndex; j++) {
                if (rowIndex == j) {
                    double ii = matrix[rowIndex][rowIndex];
                    unit = unit && ii == 1;
                } else {
                    double ij = matrix[rowIndex][j], ji = matrix[j][rowIndex];

                    diagonal = diagonal && ij == 0 && ji == 0;
                    symmetrical = symmetrical && ij == ji;
                    upper = upper && ij == 0;
                    lower = lower && ji == 0;
                }
            }
        }

        if(upper || lower) {
            TriangularMatrixType triangularMatrixType = upper ? TriangularMatrixType.UpperTriangular : TriangularMatrixType.LowerTriangular;
            MatrixDiagonalType matrixDiagonalType = unit ? MatrixDiagonalType.Unit : MatrixDiagonalType.NonUnit;
            return new DenseTriangularMatrix(elementAccessor, triangularMatrixType, matrixDiagonalType);
        }

        return new DenseGeneralMatrix(elementAccessor);
    }

    public DenseMatrix multiply(DenseMatrix that) {
        final int M = this.getNumberOfRows(), N = that.getNumberOfColumns();
        DenseMatrixBuilder resultBuilder = DenseMatrixBuilder.create(M, N);

        if(that instanceof DenseTriangularMatrix) {
            blasLevel3.dtrmm(1.0, MatrixSide.RIGHT, (DenseTriangularMatrix) that, this, 0.0, resultBuilder);
        } else {
            blasLevel3.dgemm(1.0, this, that, 0.0, resultBuilder);
        }

        return resultBuilder.build();
    }

    @Override
    public MatrixType getMatrixType() {
        return MatrixType.GE;
    }

    @Override
    public int getNumberOfRows() {
        return elementAccessor.getRowCount();
    }

    @Override
    public int getNumberOfColumns() {
        return elementAccessor.getColumnCount();
    }

    @Override
    public final double get(int rowIndex, int columnIndex) {
        return elementAccessor.get(rowIndex, columnIndex);
    }

    @Override
    public MatrixColumn getColumn(int columnIndex) {
        return new DenseMatrixColumn(elementAccessor.getColumn(columnIndex), columnIndex);
    }

    @Override
    public MatrixRow getRow(int rowIndex) {
        return new DenseMatrixRow(elementAccessor.getRow(rowIndex), rowIndex);
    }

    @Override
    public DenseMatrix slice(int rowStart, int rowEnd, int columnStart, int columnEnd) {
        return create(elementAccessor.slice(rowStart, rowEnd, columnStart, columnEnd));
    }

    @Override
    public DenseMatrix transpose() {
        return create(elementAccessor.transpose());
    }

    @Override
    public DenseMatrix reshape(int rowCount, int columnCount) {
        return create(elementAccessor.reshape(rowCount, columnCount));
    }

    @Override
    public DenseMatrix compact() {
        return create(elementAccessor.compact());
    }

    public double[] toArray(ArrayElementOrder elementOrder) {
        return elementAccessor.toArray(elementOrder);
    }

    public void toArray(double[] copy, ArrayElementOrder elementOrder) {
        elementAccessor.toArray(copy, elementOrder);
    }

    private static final class DenseMatrixRow extends DenseVector implements MatrixRow {
        private static final long serialVersionUID = -3104116423696752144L;
        private final int index;

        private DenseMatrixRow(ArrayElementAccessor1D elementAccessor, int index) {
            super(elementAccessor);
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }

    private static final class DenseMatrixColumn extends DenseVector implements MatrixColumn {
        private static final long serialVersionUID = 7774119470007792583L;
        private final int index;

        private DenseMatrixColumn(ArrayElementAccessor1D elementAccessor, int index) {
            super(elementAccessor);
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }

    public void setBlasLevel3(BLASLevel3<DenseMatrix, DenseTriangularMatrix, DenseSymmetricMatrix, DenseMatrixBuilder> blasLevel3) {
        this.blasLevel3 = blasLevel3;
    }
}
