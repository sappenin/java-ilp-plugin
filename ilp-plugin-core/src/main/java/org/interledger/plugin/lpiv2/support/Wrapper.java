package org.interledger.plugin.lpiv2.support;

import org.immutables.value.Value;

/**
 * A class helper for creating type-safe wrappers using Immutables.
 *
 * @see "http://immutables.github.io/immutable.html#wrapper-types"
 */
public abstract class Wrapper<T> {

    @Value.Parameter
    public abstract T value();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + value() + ")";
    }
}