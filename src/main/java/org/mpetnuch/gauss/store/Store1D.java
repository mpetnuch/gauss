package org.mpetnuch.gauss.store;


/**
 * @author Michael Petnuch
 * @version $Id$
 */
public interface Store1D extends Store {
    double get(int index);

    Store1D compact();

    Store1D slice(int startIndex, int endIndex);

    Store2D reshape(int rowCount, int columnCount);
}
