/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStructure.java` is part of Gauss.
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

package org.mpetnuch.gauss.structure.array;


import org.mpetnuch.gauss.structure.Slice;
import org.mpetnuch.gauss.structure.Structure;

/**
 * @author Michael Petnuch
 */
public interface ArrayStructure extends Structure {
    int NO_UNIT_STRIDE_DIMENSION = 1;

    @Override
    ArrayStructure swapAxis(int axis1, int axis2);

    @Override
    ArrayStructure slice(Slice... slices);

    int lastIndex();

    int index(int ordinal);

    int index(int... indices);

    int[] indices(int ordinal);

    int ordinal(int... indices);

    int offset();

    int stride(int dimension);

    int backstride(int dimension);

    boolean isContiguous();

    int unitStrideDimension();

    default boolean hasUnitStrideDimension() {
        return NO_UNIT_STRIDE_DIMENSION != unitStrideDimension();
    }
}
