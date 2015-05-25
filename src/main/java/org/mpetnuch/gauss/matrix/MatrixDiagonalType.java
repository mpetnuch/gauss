/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `MatrixDiagonalType.java` is part of Gauss.
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

package org.mpetnuch.gauss.matrix;

/**
 * Indicates whether a triangular matrix is unit-diagonal (diagonal elements are all equal to 1).
 *
 * @author Michael Petnuch
 * @version $Id$
 */
public enum MatrixDiagonalType {
    NonUnit(132), Unit(131);

    private final int type;

    MatrixDiagonalType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
