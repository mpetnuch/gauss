/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `Structure2D.java` is part of Gauss.
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

package org.mpetnuch.gauss.structure;

import org.mpetnuch.gauss.exception.InvalidRangeException;

/**
 * @author Michael Petnuch
 */
public interface Structure2D extends Structure {
    int ROW_DIMENSION = 0;
    int COLUMN_DIMENSION = 1;

    Structure2D transpose();

    Structure1D column(int columnIndex);

    Structure1D row(int rowIndex);

    int rowCount();

    int columnCount();

    @Override
    default int size() {
        return rowCount() * columnCount();
    }

    @Override
    default int dimension() {
        return 2;
    }

    @Override
    default int dimensionLength(int dimension) {
        switch (dimension) {
            case ROW_DIMENSION:
                return rowCount();
            case COLUMN_DIMENSION:
                return columnCount();
        }

        throw new InvalidRangeException(dimension, 0, 2);
    }
}
