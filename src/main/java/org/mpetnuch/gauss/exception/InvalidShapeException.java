/*
 * Copyright (c) 2015, Michael Petnuch. All Rights Reserved.
 *
 * This file `InvalidShapeException.java` is part of Gauss.
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
import java.util.Arrays;

/**
 * @author Michael Petnuch
 */
public class InvalidShapeException extends IllegalArgumentException {
    private static final long serialVersionUID = -5075373805864191622L;

    private final int[] desired;
    private final int[] current;

    public InvalidShapeException(int desired, int current) {
        this.desired = new int[]{desired};
        this.current = new int[]{current};
    }

    public InvalidShapeException(int[] desired, int[] current) {
        this.desired = desired;
        this.current = current;
    }
    //ValueError: total size of new array must be unchanged

    @Override
    public String getMessage() {
        return MessageFormat.format(
                "desired shape {0} incompatible with current shape {1}",
                Arrays.toString(desired), Arrays.toString(current));
    }
}
