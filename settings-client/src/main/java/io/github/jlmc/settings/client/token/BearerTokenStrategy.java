package io.github.jlmc.settings.client.token;

import io.github.jlmc.settings.client.auth.BearerTokenCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Token acquisition strategy for direct Bearer Token credentials.
///
/// This strategy simply returns the provided token without performing any remote acquisition
/// or transformation. Use this for scenarios where the token is already available and valid.
public class BearerTokenStrategy implements TokenAcquisitionStrategy<BearerTokenCredentials> {

    private static final Logger logger = LoggerFactory.getLogger(BearerTokenStrategy.class);

    @Override
    public Class<BearerTokenCredentials> getSupportedType() {
        return BearerTokenCredentials.class;
    }

    /**
     * Returns the token from the provided BearerTokenCredentials without modification.
     */
    @Override
    public String acquireToken(BearerTokenCredentials credentials) {
        logger.debug("Using direct Bearer Token for client. No acquisition needed.");
        return credentials.token();
    }
}
