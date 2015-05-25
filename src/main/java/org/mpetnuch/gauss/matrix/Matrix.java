/*
 * Copyright (c) 2014, Michael Petnuch. All Rights Reserved.
 *
 * This file `Matrix.java` is part of Gauss.
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

package org.mpetnuch.gauss.matrix;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public interface Matrix extends Serializable {

    int getNumberOfRows();

    MatrixRow getRow(int rowIndex);

    int getNumberOfColumns();

    MatrixColumn getColumn(int columnIndex);

    Matrix transpose();

    Matrix compact();

    default Stream<MatrixRow> rows() {
        return IntStream.range(0, getNumberOfRows()).mapToObj(this::getRow);
    }

    default Stream<MatrixColumn> columns() {
        return IntStream.range(0, getNumberOfColumns()).mapToObj(this::getColumn);
    }

    Matrix slice(int rowStart, int rowEnd, int columnStart, int columnEnd);

    MatrixType getMatrixType();

    double get(int rowIndex, int columnIndex);

    default int getMatrixSize() {
        return this.getNumberOfRows() * this.getNumberOfColumns();
    }

    default double[][] toArray() {
        return rows().map(v -> v.stream().toArray()).toArray(double[][]::new);
    }

    default void print() {
        print(System.out);
    }

    default void print(PrintStream out) {
        final int rowCount = getNumberOfRows();
        final int columnCount = getNumberOfColumns();

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                out.printf(" %.5f  ", get(rowIndex, columnIndex));
            }

            out.println();
        }
    }

    interface MatrixRow extends Vector {
        int getIndex();
    }

    interface MatrixColumn extends Vector {
        int getIndex();
    }
}
