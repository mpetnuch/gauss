/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `MatrixBuilder.java` is part of Gauss.
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
 * @author Michael Petnuch
 * @version $Id$
 */

public interface MatrixBuilder<T extends Matrix, S extends MatrixBuilder<T, S>> {
    S scale(double alpha);

    S add(int rowIndex, int columnIndex, double alpha);

    S set(int rowIndex, int columnIndex, double alpha);

    S slice(int rowIndexStart, int rowIndexEnd, int columnIndexStart, int columnIndexEnd);

    T build();
}
