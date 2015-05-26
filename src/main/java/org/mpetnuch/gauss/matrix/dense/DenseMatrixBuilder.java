/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `DenseMatrixBuilder.java` is part of Gauss.
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

import org.mpetnuch.gauss.matrix.MatrixBuilder;
import org.mpetnuch.gauss.store.array.MutableArrayStore2D;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class DenseMatrixBuilder implements MatrixBuilder<DenseMatrix, DenseMatrixBuilder> {
    private final MutableArrayStore2D store;

    private DenseMatrixBuilder(MutableArrayStore2D store) {
        this.store = store;
    }

    public DenseMatrixBuilder(int rowCount, int columnCount) {
        this.store = new MutableArrayStore2D(rowCount, columnCount);
    }

    @Override
    public DenseMatrixBuilder scale(double alpha) {
        if (Double.compare(alpha, 0.0) == 0) {
            return this;
        } else if (Double.compare(1.0, alpha) == 0) {
            return this;
        }

        store.replaceAll(x -> x * alpha);
        return this;
    }

    @Override
    public DenseMatrixBuilder add(int rowIndex, int columnIndex, double alpha) {
        store.increment(rowIndex, columnIndex, alpha);
        return this;
    }

    public DenseMatrixBuilder set(int rowIndex, int columnIndex, double alpha) {
        store.set(rowIndex, columnIndex, alpha);
        return this;
    }

    @Override
    public DenseMatrixBuilder slice(int rowIndexStart, int rowIndexEnd, int columnIndexStart, int columnIndexEnd) {
        return new DenseMatrixBuilder(store.slice(rowIndexStart, rowIndexEnd, columnIndexStart, columnIndexEnd));
    }

    @Override
    public DenseMatrix build() {
        return new DenseGeneralMatrix(store.immutableCopy());
    }
}
