package org.adridadou.ethereum.values;

/**
 * Created by davidroon on 12.03.17.
 * This code is released under Apache 2 license
 */
public interface IPayable<T> {
    Object with(EthValue value);
}
