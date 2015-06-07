/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `Store2D.java` is part of Gauss.
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

package org.mpetnuch.gauss.store;

import java.util.PrimitiveIterator;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public interface Store2D extends Store {
    double get(int rowIndex, int columnIndex);

    int rowCount();

    int columnCount();

    Store2D transpose();

    Store2D slice(int rowStartInclusive, int rowEndExclusive,
                  int columnStartInclusive, int columnEndExclusive);

    Store1D column(int columnIndex);

    Store1D row(int rowIndex);

    default double[][] toArray2D() {
        final PrimitiveIterator.OfDouble iterator = iterator();
        final double[][] array = new double[rowCount()][columnCount()];

        for (int i = 0, rowCount = rowCount(); i < rowCount; i++) {
            for (int j = 0, columnCount = columnCount(); j < columnCount; j++) {
                array[i][j] = iterator.nextDouble();
            }
        }

        return array;
    }
}
