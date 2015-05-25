package org.mpetnuch.gauss.store;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public enum ArrayElementOrder {
    RowMajor(0), ColumnMajor(1);

    private final int strideDimension;

    ArrayElementOrder(int strideDimension) {
        this.strideDimension = strideDimension;
    }

    public int getStrideDimension() {
        return strideDimension;
    }
}
