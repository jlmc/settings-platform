package io.github.jlmc.settings.client.adapters.http;

public interface ClientHttpExecutor extends AutoCloseable {

    ClientHttpResponse<String> send(ClientHttpRequest clientHttpRequest);
}
