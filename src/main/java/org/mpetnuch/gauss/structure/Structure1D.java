/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `Structure1D.java` is part of Gauss.
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

import org.mpetnuch.gauss.exception.InvalidDimensionRangeException;

/**
 * @author Michael Petnuch
 */
public interface Structure1D extends Structure {
    int length();

    @Override
    default int size() {
        return length();
    }

    @Override
    default int dimension() {
        return 1;
    }

    @Override
    default int dimensionLength(int dimension) {
        if (dimension == 0) {
            return length();
        }

        throw new InvalidDimensionRangeException(dimension, 0, 0);
    }

    Structure1D slice(Slice slice);
}
