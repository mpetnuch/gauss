/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `Vector.java` is part of Gauss.
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

import java.io.Serializable;
import java.util.stream.DoubleStream;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public interface Vector extends Serializable {
    double get(int i);

    int size();

    Matrix reshape(int rows, int columns);

    Vector slice(int startInclusive, int endExclusive);

    Vector compact();

    DoubleStream stream();

    double[] toArray();

    void copyInto(double[] copy, int offset);

    void copyInto(double[] copy);

    default Matrix toColumnMatrix() {
        return reshape(size(), 1);
    }

    default Matrix toRowMatrix() {
        return reshape(1, size());
    }
}
