package org.mpetnuch.gauss.matrix.dense;

import org.mpetnuch.gauss.matrix.SymmetricMatrix;
import org.mpetnuch.gauss.matrix.accessor.ArrayElementAccessor2D;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class DenseSymmetricMatrix extends DenseMatrix implements SymmetricMatrix {
    private static final long serialVersionUID = -3530723819135774578L;

    DenseSymmetricMatrix(ArrayElementAccessor2D elementAccessor) {
        super(elementAccessor);
    }

    @Override
    DenseMatrix create(ArrayElementAccessor2D elementAccessor) {
        return new DenseSymmetricMatrix(elementAccessor);
    }
}
