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

    public Object getProxy() {
        return proxy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProxyWrapper that = (ProxyWrapper) o;

        return proxy != null ? proxy == that.proxy : that.proxy == null;

    }

    @Override
    public int hashCode() {
        return 0;
    }
}
