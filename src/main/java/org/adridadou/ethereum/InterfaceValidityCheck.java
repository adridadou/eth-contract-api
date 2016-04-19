package org.adridadou.ethereum;

import org.ethereum.core.CallTransaction;

/**
 * Created by davidroon on 31.03.16.
 * This code is released under Apache 2 license
 */
public class InterfaceValidityCheck {
    private final Class<?> ethInterface;
    private final CallTransaction.Contract contract;

    public InterfaceValidityCheck(Class<?> ethInterface, CallTransaction.Contract contract) {
        this.ethInterface = ethInterface;
        this.contract = contract;
    }

    public static InterfaceValidityCheck check(Class<?> ethInterface, CallTransaction.Contract contract) {
        return new InterfaceValidityCheck(ethInterface, contract);
    }

    public InterfaceValidityCheck checkMethods() {

        return this;
    }

    public InterfaceValidityCheck checkReturnType() {
        return this;
    }
}
