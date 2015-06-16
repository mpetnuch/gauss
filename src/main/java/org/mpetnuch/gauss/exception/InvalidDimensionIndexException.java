/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `InvalidDimensionIndexException.java` is part of Gauss.
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

package org.mpetnuch.gauss.exception;

import java.text.MessageFormat;

/**
 * @author Michael Petnuch
 */
public class InvalidDimensionIndexException extends IllegalArgumentException {
    private static final long serialVersionUID = 412946644176401970L;

    private final int index;
    private final int dimension;
    private final int length;

    public InvalidDimensionIndexException(int index, int dimension, int length) {
        this.index = index;
        this.dimension = dimension;
        this.length = length;
    }

    @Override
    public String getMessage() {
        return MessageFormat.format("index {0} is out of bounds for dimension {1} with length {2}",
                index, dimension, length);
    }
}
