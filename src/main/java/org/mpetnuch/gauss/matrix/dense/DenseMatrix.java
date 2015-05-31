/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `DenseMatrix.java` is part of Gauss.
 *
 * Gauss is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.mpetnuch.gauss.matrix.dense;

import org.mpetnuch.gauss.linearalgebra.blas3.BLASLevel3;
import org.mpetnuch.gauss.linearalgebra.blas3.JBLASLevel3;
import org.mpetnuch.gauss.matrix.Matrix;
import org.mpetnuch.gauss.matrix.MatrixDiagonalType;
import org.mpetnuch.gauss.matrix.MatrixType;
import org.mpetnuch.gauss.matrix.TriangularMatrixType;
import org.mpetnuch.gauss.store.array.ArrayStore1D;
import org.mpetnuch.gauss.store.array.ArrayStore2D;
import org.mpetnuch.gauss.store.array.ArrayStore2D.ArrayStructure2D;
import org.mpetnuch.gauss.store.array.ArrayStore2D.RowMajorArrayStructure2D;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public abstract class DenseMatrix implements Matrix {
    private static final long serialVersionUID = -7150013139589271348L;
    private static final JBLASLevel3 DEFAULT_BLAS_LEVEL3 = new JBLASLevel3.JBLASLevel3Builder().createJBLASLevel3();

    protected final ArrayStore2D store;
    protected BLASLevel3<DenseMatrix, DenseTriangularMatrix, DenseSymmetricMatrix, DenseMatrixBuilder> blasLevel3 = DEFAULT_BLAS_LEVEL3;

    DenseMatrix(ArrayStore2D store) {
        this.store = store;
    }

    public static DenseMatrix from(double[][] matrix) {
        final int rowCount = matrix.length;
        final int columnCount = matrix[0].length;
        for (int i = 1; i < rowCount; i++) {
            if (matrix[i].length != columnCount) {
                throw new IllegalArgumentException("Not a matrix, all rows must be same size.");
            }
        }

        final ArrayStructure2D structure = new RowMajorArrayStructure2D(rowCount, columnCount);
        if (rowCount != columnCount) {
            double[] flattenedArray = new double[rowCount * columnCount];
            for (int index = 0, rowIndex = 0; rowIndex < rowCount; rowIndex++, index += columnCount) {
                System.arraycopy(matrix[rowIndex], 0, flattenedArray, index, columnCount);
            }

            final ArrayStore2D arrayStore = new ArrayStore2D(flattenedArray, structure);
            return new DenseGeneralMatrix(arrayStore);
        }

        final double[] elements = new double[rowCount * columnCount];

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

        final ArrayStore2D arrayStore = new ArrayStore2D(elements, structure);
        if (upper || lower) {
            final TriangularMatrixType triangularMatrixType = upper ?
                    TriangularMatrixType.UpperTriangular : TriangularMatrixType.LowerTriangular;

            final MatrixDiagonalType matrixDiagonalType = unit ?
                    MatrixDiagonalType.Unit : MatrixDiagonalType.NonUnit;

            return new DenseTriangularMatrix(arrayStore, triangularMatrixType, matrixDiagonalType);
        }

        if (symmetrical) {
            return new DenseSymmetricMatrix(arrayStore);
        }

        return new DenseGeneralMatrix(arrayStore);
    }

    abstract DenseMatrix create(ArrayStore2D elementAccessor);

    public DenseMatrix multiply(DenseMatrix that) {
        final int M = this.getNumberOfRows(), N = that.getNumberOfColumns();
        final DenseMatrixBuilder resultBuilder = new DenseMatrixBuilder(M, N);

        blasLevel3.dgemm(1.0, this, that, 0.0, resultBuilder);
        return resultBuilder.build();
    }

    @Override
    public MatrixType getMatrixType() {
        return MatrixType.GE;
    }

    @Override
    public int getNumberOfRows() {
        return store.structure().rowCount();
    }

    @Override
    public int getNumberOfColumns() {
        return store.structure().columnCount();
    }

    @Override
    public final double get(int rowIndex, int columnIndex) {
        return store.get(rowIndex, columnIndex);
    }

    @Override
    public MatrixColumn getColumn(int columnIndex) {
        return new DenseMatrixColumn(store.column(columnIndex), columnIndex);
    }

    @Override
    public MatrixRow getRow(int rowIndex) {
        return new DenseMatrixRow(store.row(rowIndex), rowIndex);
    }

    @Override
    public DenseMatrix slice(int rowStart, int rowEnd, int columnStart, int columnEnd) {
        return create(store.slice(rowStart, rowEnd, columnStart, columnEnd));
    }

    @Override
    public DenseMatrix transpose() {
        return create(store.transpose());
    }

    @Override
    public DenseMatrix compact() {
        if(store.isCompact()) {
            return this;
        } else {
            return create(store.compact());
        }
    }

    public void setBlasLevel3(BLASLevel3<DenseMatrix, DenseTriangularMatrix, DenseSymmetricMatrix, DenseMatrixBuilder> blasLevel3) {
        this.blasLevel3 = blasLevel3;
    }

    private static final class DenseMatrixRow extends DenseVector implements MatrixRow {
        private static final long serialVersionUID = -3104116423696752144L;
        private final int index;

        private DenseMatrixRow(ArrayStore1D elementAccessor, int index) {
            super(elementAccessor);
            this.index = index;
        }

        @Override
        public int getRowIndex() {
            return index;
        }
    }

    private static final class DenseMatrixColumn extends DenseVector implements MatrixColumn {
        private static final long serialVersionUID = 7774119470007792583L;
        private final int index;

        private DenseMatrixColumn(ArrayStore1D elementAccessor, int index) {
            super(elementAccessor);
            this.index = index;
        }

        @Override
        public int getColumnIndex() {
            return index;
        }
    }
}
