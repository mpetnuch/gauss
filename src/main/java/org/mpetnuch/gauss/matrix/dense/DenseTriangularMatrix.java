/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `DenseTriangularMatrix.java` is part of Gauss.
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

import org.mpetnuch.gauss.matrix.MatrixDiagonalType;
import org.mpetnuch.gauss.matrix.MatrixSide;
import org.mpetnuch.gauss.matrix.TriangularMatrix;
import org.mpetnuch.gauss.matrix.TriangularMatrixType;
import org.mpetnuch.gauss.store.ArrayStore2D;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class DenseTriangularMatrix extends DenseMatrix implements TriangularMatrix {
    private static final long serialVersionUID = 7525387465248808621L;

    private final TriangularMatrixType triangularMatrixType;
    private final MatrixDiagonalType matrixDiagonalType;

    DenseTriangularMatrix(ArrayStore2D elementStore, TriangularMatrixType triangularMatrixType, MatrixDiagonalType matrixDiagonalType) {
        super(elementStore);
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
        return create(elementStore.slice(rowStart, rowEnd, rowStart, rowEnd));
    }

    @Override
    DenseTriangularMatrix create(ArrayStore2D elementAccessor) {
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
