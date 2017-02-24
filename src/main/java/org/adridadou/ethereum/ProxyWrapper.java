package org.adridadou.ethereum;

/**
 * Created by davidroon on 21.09.16.
 * This code is released under Apache 2 license
 */
public class ProxyWrapper {
    private final Object proxy;

    public ProxyWrapper(Object proxy) {
        this.proxy = proxy;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ProxyWrapper && this.proxy == ((ProxyWrapper) o).proxy;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
