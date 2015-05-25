package org.mpetnuch.gauss.matrix.dense;

import org.mpetnuch.gauss.matrix.Matrix;
import org.mpetnuch.gauss.matrix.Vector;
import org.mpetnuch.gauss.store.ArrayStore1D;

import java.util.stream.DoubleStream;

/**
 * @author Michael Petnuch
 * @version $Id$
 */
public class DenseVector implements Vector  {
    private static final long serialVersionUID = 1577827666390044084L;
    private final ArrayStore1D elementAccessor;

    public DenseVector(ArrayStore1D elementAccessor) {
        this.elementAccessor = elementAccessor;
    }

    @Override
    public double get(int i) {
        return elementAccessor.get(i);
    }

    @Override
    public int length() {
        return elementAccessor.getStructure().size();
    }

    @Override
    public Matrix reshape(int rowCount, int columnCount) {
        return new DenseGeneralMatrix(elementAccessor.reshape(rowCount, columnCount));
    }

    @Override
    public Vector slice(int startIndex, int endIndex) {
        return new DenseVector(elementAccessor.slice(startIndex, endIndex));
    }

    @Override
    public Vector compact() {
        return new DenseVector(elementAccessor.compact());
    }

    @Override
    public DoubleStream stream() {
        return elementAccessor.stream();
    }

    @Override
    public double[] toArray() {
        return elementAccessor.toArray();
    }

    @Override
    public void copyInto(double[] copy) {
        elementAccessor.copyInto(copy);
    }
}
