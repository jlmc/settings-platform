package io.github.jlmc.poc.commons.settings.http;

public interface ClientHttpExecutor extends AutoCloseable {

    ClientHttpResponse<String> send(ClientHttpRequest clientHttpRequest);
}
