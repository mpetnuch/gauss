/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `DenseVector.java` is part of Gauss.
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

import org.mpetnuch.gauss.matrix.Vector;
import org.mpetnuch.gauss.store.array.ArrayStore1D;

import java.util.stream.DoubleStream;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class DenseVector implements Vector  {
    private static final long serialVersionUID = 1577827666390044084L;
    private final ArrayStore1D elementAccessor;

    public DenseVector(ArrayStore1D elementAccessor) {
        this.elementAccessor = elementAccessor;
    }

    @Override
    public double get(int i) {
        return elementAccessor.get(i);
    }

    @Override
    public int size() {
        return elementAccessor.getStructure().size();
    }

    @Override
    public DenseMatrix reshape(int rowCount, int columnCount) {
        return new DenseGeneralMatrix(elementAccessor.reshape(rowCount, columnCount));
    }

    @Override
    public Vector slice(int startIndex, int endIndex) {
        return new DenseVector(elementAccessor.slice(startIndex, endIndex));
    }

    @Override
    public Vector compact() {
        return new DenseVector(elementAccessor.compact());
    }

    @Override
    public DoubleStream stream() {
        return elementAccessor.stream();
    }

    @Override
    public double[] toArray() {
        return elementAccessor.toArray();
    }

    @Override
    public void copyInto(double[] copy) {
        elementAccessor.copyInto(copy);
    }

    @Override
    public void copyInto(double[] copy, int offset) {
        elementAccessor.copyInto(copy, offset);
    }
}
