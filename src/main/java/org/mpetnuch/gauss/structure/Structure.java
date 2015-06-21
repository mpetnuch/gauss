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

import org.mpetnuch.gauss.exception.InvalidDimensionIndexException;
import org.mpetnuch.gauss.exception.InvalidRangeException;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public interface Structure {
    int size();

    int dimension();

    int dimensionLength(int dimension);

    default int[] shape() {
        return dimensions().mapToInt(Dimension::length).toArray();
    }

    default Dimension dimension(final int dimension) {
        if (dimension < 0 || dimension > dimension()) {
            throw new InvalidRangeException(dimension, 0, dimension());
        }

        return new Dimension() {
            @Override
            public int dimensionIndex() {
                return dimension;
            }

            @Override
            public int length() {
                return dimensionLength(dimension);
            }

            @Override
            public int index(final int position) {
                final int length = length();
                final int index = position < 0 ? position + length : position;
                if (index < length) {
                    return index;
                }

                throw new InvalidDimensionIndexException(position, dimension, length);
            }
        };
    }

    default Stream<Dimension> dimensions() {
        return IntStream.range(0, dimension()).mapToObj(this::dimension);
    }

    Structure swapAxis(int axis1, int axis2);

    Structure slice(Slice... slices);
}
