package org.mpetnuch.gauss.matrix;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public enum MatrixSide {
    LEFT(141),
    RIGHT(142);

    private final int type;

    MatrixSide(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}