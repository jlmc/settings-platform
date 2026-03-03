package io.github.jlmc.settings.client.adapters.token;

import io.github.jlmc.settings.client.core.auth.AuthCredentials;
import io.github.jlmc.settings.client.core.exceptions.SettingsClientException;
import io.github.jlmc.settings.client.ports.out.AccessTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Central manager for acquiring OAuth2 access tokens using various credential types.
 * Delegates token acquisition to the appropriate TokenAcquisitionStrategy based on the type of credentials.
 */
public class TokenOrchestrator implements AccessTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenOrchestrator.class);

    private final Map<Class<? extends AuthCredentials>, TokenAcquisitionStrategy<? extends AuthCredentials>> strategiesMap;

    public TokenOrchestrator(List<TokenAcquisitionStrategy<? extends AuthCredentials>> strategies) {
        this.strategiesMap = strategies.stream()
                .collect(Collectors.toMap(TokenAcquisitionStrategy::getSupportedType, strategy -> strategy));
    }

    @Override
    public String acquireToken(AuthCredentials credentials) {
        TokenAcquisitionStrategy<? extends AuthCredentials> strategy =
                strategiesMap.get(credentials.getClass());

        if (strategy == null) {
            throw new SettingsClientException("Unsupported credential type: " + credentials.getClass().getSimpleName());
        }

        logger.debug(
                "Acquiring token using strategy {} for credential type {}",
                strategy.getClass().getSimpleName(),
                credentials.getClass().getSimpleName()
        );

        @SuppressWarnings("unchecked")
        TokenAcquisitionStrategy<AuthCredentials> castedStrategy = (TokenAcquisitionStrategy<AuthCredentials>) strategy;

        return castedStrategy.acquireToken(credentials);
    }
}
