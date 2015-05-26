/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `MutableArrayStore2D.java` is part of Gauss.
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

package org.mpetnuch.gauss.store.array;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class MutableArrayStore2D extends ArrayStore2D {

    public MutableArrayStore2D(ArrayStructure2D structure) {
        super(new double[structure.size()], structure);
    }

    public MutableArrayStore2D(int rowCount, int columnCount) {
        super(new double[rowCount * columnCount], new RowMajorArrayStructure2D(rowCount, columnCount));
    }

    public MutableArrayStore2D(double[] array, ArrayStructure2D structure) {
        super(array, structure);
    }

    @Override
    public MutableArrayStore2D slice(int rowIndexStart, int rowIndexEnd, int columnIndexStart, int columnIndexEnd) {
        return new MutableArrayStore2D(array, structure.slice(rowIndexStart, rowIndexEnd, columnIndexStart, columnIndexEnd));
    }

    public void set(int rowIndex, int columnIndex, double value) {
        array[structure.index(rowIndex, columnIndex)] = value;
    }

    public void increment(int rowIndex, int columnIndex, double value) {
        array[structure.index(rowIndex, columnIndex)] += value;
    }

    public void replaceAll(DoubleUnaryOperator operator) {
        Arrays.setAll(array, operator::applyAsDouble);
    }

    public ArrayStore2D immutableCopy() {
        return new ArrayStore2D(array.clone(), structure);
    }
}
