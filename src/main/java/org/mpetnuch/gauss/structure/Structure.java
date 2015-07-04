/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `Structure.java` is part of Gauss.
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
import org.mpetnuch.gauss.exception.InvalidRangeException;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public interface Structure {
    /**
     * Returns the number of elements defined by this structure. If this structure contains
     * more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements defined by the structure
     */
    int size();

    /**
     * Returns the number of dimensions defined by this structure. If this structure contains
     * more than <tt>Integer.MAX_VALUE</tt> dimensions, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of dimensions defined by the structure
     */
    int dimension();

    /**
     * Returns the number of elements defined by the requested dimension in this structure. If this
     * structure contains more than <tt>Integer.MAX_VALUE</tt> in the requested dimensions, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements defined by the structure dimension
     * @throws InvalidRangeException if the index is out of range
     *                               (<tt>Math.abs(index) &gt; dimension()</tt>)
     */
    int dimensionLength(int dimension);

    /**
     * Returns the shape of this structure, which corresponds to the length of the dimension for each of the
     * dimensions defined by the structure.
     *
     * @return the an array corresponding to the dimension length of the each of the structure dimensions
     */
    default int[] shape() {
        return dimensions().stream().mapToInt(Dimension::length).toArray();
    }

    /**
     * Returns the shape of this structure, which corresponds to the length of the dimension for each of the
     * dimensions defined by the structure.
     *
     * @return the Dimension object corresponding to the requested dimension
     * @throws InvalidRangeException if the dimension is out of range
     *                               (<tt>Math.abs(dimension) &gt; dimension()</tt>)
     */
    default Dimension dimension(final int dimension) {
        final int dimensionIndex = dimension < 0 ? dimension + dimension() : dimension;
        if (dimensionIndex >= dimension()) {
            throw new InvalidRangeException(dimension, dimension());
        }

        final int dimensionLength = dimensionLength(dimensionIndex);

        return new Dimension() {
            @Override
            public int dimensionIndex() {
                return dimensionIndex;
            }

            @Override
            public int length() {
                return dimensionLength;
            }

            @Override
            public int index(final int position) {
                final int index = position < 0 ? position + dimensionLength : position;
                if (index < dimensionLength) {
                    return index;
                }

                throw new InvalidDimensionRangeException(position, dimensionIndex, dimensionLength);
            }
        };
    }

    /**
     * Returns a list of Dimension objects corresponding to the dimensions of the structure
     *
     * @return the a list corresponding to the dimensions of the structure
     */
    default List<Dimension> dimensions() {
        return IntStream.range(0, dimension()).mapToObj(this::dimension).collect(toList());
    }

    /**
     * Creates a new structure in which the requested axises are switched
     *
     * @param axis1
     * @param axis2
     * @return
     */
    Structure swapAxis(int axis1, int axis2);

    /**
     * Creates a new structure defines by the requested slices
     *
     * @param slices
     * @return
     */
    Structure slice(Slice... slices);
}
