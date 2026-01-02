package io.github.jlmc.settings.client.http;

public interface ClientHttpExecutor extends AutoCloseable {

    ClientHttpResponse<String> send(ClientHttpRequest clientHttpRequest);
}
