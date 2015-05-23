package org.mpetnuch.gauss.store;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public interface Store2D extends Store {
    double get(int rowIndex, int columnIndex);
}
