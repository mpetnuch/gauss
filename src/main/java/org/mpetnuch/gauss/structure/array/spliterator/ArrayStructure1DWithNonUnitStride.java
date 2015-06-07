/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `ArrayStructure1DWithNonUnitStride.java` is part of Gauss.
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

package org.mpetnuch.gauss.structure.array.spliterator;

import org.mpetnuch.gauss.structure.array.ArrayStructure1D;

/**
 * @author Michael Petnuch
 */
public final class ArrayStructure1DWithNonUnitStride extends AbstractArrayStructureSpliterator<ArrayStructure1D> {
    private final int stride = structure.stride();

    private ArrayStructure1DWithNonUnitStride(ArrayStructure1D structure, double[] array, int index, int fence) {
        super(structure, array, index, fence);
    }

    public ArrayStructure1DWithNonUnitStride(ArrayStructure1D structure, double[] array) {
        super(structure, array);
    }

    @Override
    public OfDouble trySplit() {
        final int lo = index, mid = (lo + fence) >>> 1;
        if (lo < mid) {
            return new ArrayStructure1DWithNonUnitStride(structure, array, lo, mid);
        } else {
            // can't split any more
            return null;
        }
    }

    @Override
    int nextArrayIndex(int currentArrayIndex) {
        return currentArrayIndex + stride;
    }
}
