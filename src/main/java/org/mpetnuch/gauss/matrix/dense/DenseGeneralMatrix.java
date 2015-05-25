package org.mpetnuch.gauss.matrix.dense;

import org.mpetnuch.gauss.store.ArrayStore2D;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class DenseGeneralMatrix extends DenseMatrix {
    private static final long serialVersionUID = -1336404666416014744L;

    public DenseGeneralMatrix(ArrayStore2D elementAccessor) {
        super(elementAccessor);
    }

    @Override
    DenseGeneralMatrix create(ArrayStore2D elementAccessor) {
        return new DenseGeneralMatrix(elementAccessor);
    }

}
