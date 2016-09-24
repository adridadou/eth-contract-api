package org.adridadou.ethereum.ethereumj.config;

import java.util.Optional;

/**
 * Created by davidroon on 18.09.16.
 * This code is released under Apache 2 license
 *
 * # structured trace
 * # is the trace being
 * # collected in the
 * # form of objects and
 * # exposed to the user
 * # in json or any other
 * # convenient form.
 * vm.structured {
 * trace = false
 * dir = vmtrace
 * compressed = true
 * initStorageLimit = 10000
 * }
 */
public class VmConfig {
    private final Boolean trace;
    private final String dir;
    private final Boolean compressed;
    private final Integer initStorageLimit;

    public VmConfig(Boolean trace, String dir, Boolean compressed, Integer initStorageLimit) {
        this.trace = trace;
        this.dir = dir;
        this.compressed = compressed;
        this.initStorageLimit = initStorageLimit;
    }

    @Override
    public String toString() {
        return "{" +
                Optional.ofNullable(trace).map(t -> "\ntrace = " + t).orElse("") +
                Optional.ofNullable(dir).map(d -> "\ndir = '" + d + '\'').orElse("") +
                Optional.ofNullable(compressed).map(c -> "\ncompressed = " + c).orElse("") +
                Optional.ofNullable(initStorageLimit).map(i -> "\ninitStorageLimit = " + i).orElse("") +
                "\n}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean trace;
        private String dir;
        private Boolean compressed;
        private Integer initStorageLimit;

        public Builder trace(Boolean trace) {
            this.trace = trace;
            return this;
        }

        public Builder dir(String dir) {
            this.dir = dir;
            return this;
        }

        public Builder compressed(Boolean compressed) {
            this.compressed = compressed;
            return this;
        }

        public Builder initStorageLimit(Integer initStorageLimit) {
            this.initStorageLimit = initStorageLimit;
            return this;
        }

        public VmConfig build() {
            return new VmConfig(trace, dir, compressed, initStorageLimit);
        }
    }

}
