package org.mpetnuch.gauss.matrix.dense;

import org.mpetnuch.gauss.matrix.SymmetricMatrix;
import org.mpetnuch.gauss.store.ArrayStore2D;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class DenseSymmetricMatrix extends DenseMatrix implements SymmetricMatrix {
    private static final long serialVersionUID = -3530723819135774578L;

    DenseSymmetricMatrix(ArrayStore2D elementStore) {
        super(elementStore);
    }

    @Override
    DenseMatrix create(ArrayStore2D elementStore) {
        return new DenseSymmetricMatrix(elementStore);
    }
}
