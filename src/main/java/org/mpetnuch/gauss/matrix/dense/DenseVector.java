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

import java.util.Spliterator;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class DenseVector implements Vector  {
    private final ArrayStore1D store;

    public DenseVector(ArrayStore1D store) {
        this.store = store;
    }

    @Override
    public double get(int i) {
        return store.get(i);
    }

    @Override
    public int size() {
        return store.size();
    }

    @Override
    public DenseMatrix reshape(int rowCount, int columnCount) {
        return new DenseGeneralMatrix(store.reshape(rowCount, columnCount));
    }

    @Override
    public Vector slice(int startInclusive, int endExclusive) {
        return new DenseVector(store.slice(startInclusive, endExclusive));
    }

    @Override
    public Vector compact() {
        return new DenseVector(store.compact());
    }

    @Override
    public Spliterator.OfDouble spliterator() {
        return store.spliterator();
    }
}
