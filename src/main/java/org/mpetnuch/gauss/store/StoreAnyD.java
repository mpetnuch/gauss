package org.mpetnuch.gauss.store;

/**
 * @author Michael Petnuch
 * @id $Id$
 */
public interface StoreAnyD extends Store {
    double get(int... indices);

    int dimension(int dimension);

    Structure getStructure();
}
