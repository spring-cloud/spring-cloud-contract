package io.coderate.accurest.dsl.internal

//TODO: Can we have different types for Client/Server (client: pattern(String), server: value(Boolean/Int)) ?
class CustomizableProperty<T, V> {

    private T client
    private V server

    void client(T client) {
        this.client = client
    }

    void server(V server) {
        this.server = server
    }

    T toClientSide() {
        return client
    }

    V toServerSide() {
        return server
    }
}

class StringCustomizableProperty extends CustomizableProperty<String, String> {
}

class SingleTypeCustomizableProperty<T> extends CustomizableProperty<T, T> {
    SingleTypeCustomizableProperty(T value) {
        client(value)
        server(value)
    }

    @Override
    public String toString() {
        return toClientSide()
    }
}
