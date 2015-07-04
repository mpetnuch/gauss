/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `InvalidDimensionRangeException.java` is part of Gauss.
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
public class InvalidDimensionRangeException extends InvalidRangeException {
    private static final long serialVersionUID = 412946644176401970L;

    private final int dimension;

    public InvalidDimensionRangeException(int argument, int dimension, int length) {
        super(argument, length);
        this.dimension = dimension;
    }

    @Override
    public String getMessage() {
        return MessageFormat.format("index {0} is out of range for dimension {1} with length {2}",
                argument, dimension, length);
    }
}
