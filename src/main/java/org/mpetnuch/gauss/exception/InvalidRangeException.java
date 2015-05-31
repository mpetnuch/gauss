/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `InvalidRangeException.java` is part of Gauss.
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
public class InvalidRangeException extends IllegalArgumentException {
    private static final long serialVersionUID = -8798552081345525331L;

    private final Number argument;
    private final Number lowerBound, upperBound;

    public InvalidRangeException(Number argument, Number lowerBound, Number upperBound) {
        this.argument = argument;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
    }

    @Override
    public String getMessage() {
        return MessageFormat.format("{0} out of [{1}, {2}] range", argument, upperBound, lowerBound);
    }
}
