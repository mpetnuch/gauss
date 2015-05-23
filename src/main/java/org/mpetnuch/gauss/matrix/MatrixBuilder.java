package org.mpetnuch.gauss.matrix;

/**
 * @author Michael Petnuch
 * @version $Id$
 */

public interface MatrixBuilder<T extends Matrix, S extends MatrixBuilder<T, S>> {
    S scale(double alpha);

    S add(int rowIndex, int columnIndex, double alpha);

    S set(int rowIndex, int columnIndex, double alpha);

    S slice(int rowIndexStart, int rowIndexEnd, int columnIndexStart, int columnIndexEnd);

    T build();
}
