package com.yvolabs.securedocs.function;

/**
 * This is a Custom Functional Consumer Interface that takes three inputs and returns No results
 *
 * @author Yvonne N
 * @version 1.0
 * @see java.util.function.BiConsumer
 * @since 07/06/2024
 */

@FunctionalInterface
public interface TriConsumer<T, U, V> {

    void accept(T t, U u, V v);

}
