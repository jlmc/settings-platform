package io.gihub.jlmc.poc.commons.settings.http;

public interface ClientHttpExecutor {

    ClientHttpResponse<String> send(ClientHttpRequest clientHttpRequest);
}
