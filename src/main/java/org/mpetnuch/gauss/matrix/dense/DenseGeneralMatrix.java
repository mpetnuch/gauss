package org.mpetnuch.gauss.matrix.dense;

import org.mpetnuch.gauss.matrix.accessor.ArrayElementAccessor2D;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class DenseGeneralMatrix extends DenseMatrix {
    private static final long serialVersionUID = -1336404666416014744L;

    public DenseGeneralMatrix(ArrayElementAccessor2D elementAccessor) {
        super(elementAccessor);
    }

    @Override
    DenseGeneralMatrix create(ArrayElementAccessor2D elementAccessor) {
        return new DenseGeneralMatrix(elementAccessor);
    }

}
