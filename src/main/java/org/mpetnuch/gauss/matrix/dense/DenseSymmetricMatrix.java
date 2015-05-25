/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `DenseSymmetricMatrix.java` is part of Gauss.
 *
 * Gauss is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
