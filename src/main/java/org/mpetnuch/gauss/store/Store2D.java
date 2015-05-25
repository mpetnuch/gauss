package org.mpetnuch.gauss.store;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public interface Store2D extends Store {
    double get(int rowIndex, int columnIndex);

    Store2D compact();

    Store2D transpose();

    Store2D slice(int rowIndexStart, int rowIndexEnd, int columnIndexStart, int columnIndexEnd);

    Store1D getColumn(int columnIndex);

    Store1D getRow(int rowIndex);
}
