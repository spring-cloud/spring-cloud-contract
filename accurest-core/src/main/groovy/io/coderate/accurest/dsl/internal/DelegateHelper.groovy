package io.coderate.accurest.dsl.internal

//TODO: There is problem with a trait usage
class DelegateHelper {

    public static <T> void delegateToClosure(@DelegatesTo(T) Closure closure, T delegate) {
        closure.delegate = delegate
        closure()
    }
}
