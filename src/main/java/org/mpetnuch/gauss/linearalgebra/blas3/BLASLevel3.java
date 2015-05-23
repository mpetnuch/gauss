/*
 * Copyright (c) 2014, Michael Petnuch. All Rights Reserved.
 *
 * This file `BLASLevel3.java` is part of Gauss.
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

package org.mpetnuch.gauss.linearalgebra.blas3;

import org.mpetnuch.gauss.matrix.Matrix;
import org.mpetnuch.gauss.matrix.MatrixBuilder;
import org.mpetnuch.gauss.matrix.MatrixSide;
import org.mpetnuch.gauss.matrix.SymmetricMatrix;
import org.mpetnuch.gauss.matrix.TriangularMatrix;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public interface BLASLevel3<General extends Matrix, Triangular extends TriangularMatrix, Symmetric extends SymmetricMatrix, Builder extends MatrixBuilder<? extends General, Builder>> {
    void dgemm(double alpha, General a, General b, double beta, Builder c);

    void dsymm(double alpha, MatrixSide matrixSide, Symmetric a, General b, double beta, Builder c);

    void dtrmm(double alpha, MatrixSide matrixSide, Triangular a, General b, double beta, Builder c);
}
