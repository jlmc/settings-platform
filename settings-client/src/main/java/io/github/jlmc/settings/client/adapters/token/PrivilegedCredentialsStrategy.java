package io.github.jlmc.settings.client.adapters.token;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.jlmc.settings.client.adapters.http.Body;
import io.github.jlmc.settings.client.adapters.http.ClientHttpRequest;
import io.github.jlmc.settings.client.adapters.http.HttpMethod;
import io.github.jlmc.settings.client.core.auth.PrivilegedCredentials;
import io.github.jlmc.settings.client.core.exceptions.SettingsClientException;
import io.github.jlmc.settings.client.ports.out.HttpExecutionStrategy;
import io.github.jlmc.settings.client.ports.out.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Token acquisition strategy for privileged credentials using JWT client assertion.
 */
public class PrivilegedCredentialsStrategy implements TokenAcquisitionStrategy<PrivilegedCredentials> {

    private static final Logger logger = LoggerFactory.getLogger(PrivilegedCredentialsStrategy.class);

    private static final String TOKEN_PATH = "/oauth/token";
    private static final String GRANT_TYPE = "client_credentials";
    private static final String ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
    private static final long TOKEN_EXPIRATION_SECONDS = 300L;

    private final HttpExecutionStrategy httpExecutionStrategy;
    private final JsonDeserializer jsonDeserializer;

    public PrivilegedCredentialsStrategy(HttpExecutionStrategy httpExecutionStrategy, JsonDeserializer jsonDeserializer) {
        this.httpExecutionStrategy = httpExecutionStrategy;
        this.jsonDeserializer = jsonDeserializer;
    }

    @Override
    public Class<PrivilegedCredentials> getSupportedType() {
        return PrivilegedCredentials.class;
    }

    @Override
    public String acquireToken(PrivilegedCredentials credentials) {
        logger.info("Requesting privileged OAuth2 token for clientId={}", credentials.clientId());

        String jwt = generateJwtToken(credentials);
        ClientHttpRequest request = buildRequest(credentials, jwt);
        String responseBody = executeHttpCall(request);

        return parseAccessToken(responseBody);
    }

    private String executeHttpCall(ClientHttpRequest request) {
        return httpExecutionStrategy.execute(request);
    }

    private ClientHttpRequest buildRequest(PrivilegedCredentials credentials, String jwt) {
        URI authUri = URI.create(credentials.engineerOauthTokenUrl() + TOKEN_PATH);

        Map<String, String> formMap = Map.of(
                "grant_type", GRANT_TYPE,
                "client_id", credentials.clientId(),
                "client_assertion_type", ASSERTION_TYPE,
                "client_assertion", jwt
        );

        return new ClientHttpRequest(
                authUri,
                HttpMethod.POST,
                Collections.emptyMap(),
                null,
                new Body.Form(formMap));
    }

    @SuppressWarnings("unchecked")
    private String parseAccessToken(String responseBody) {
        Map<String, Object> map = (Map<String, Object>) jsonDeserializer.deserialize(responseBody, Map.class);
        Object token = map.get("access_token");
        if (token instanceof String) {
            return (String) token;
        } else {
            throw new SettingsClientException("Missing access_token in token response");
        }
    }

    private String generateJwtToken(PrivilegedCredentials credentials) {
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(credentials.clientPrivateKeyB64());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            ECPrivateKey privateKey = (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(keySpec);

            ECDSASigner signer = new ECDSASigner(privateKey);

            Date now = new Date();
            Date expiration = new Date(now.getTime() + TOKEN_EXPIRATION_SECONDS * 1000);
            String jti = UUID.randomUUID().toString();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .jwtID(jti)
                    .issuer(credentials.clientId())
                    .subject(credentials.clientId())
                    .audience(credentials.engineerOauthTokenUrl())
                    .issueTime(now)
                    .expirationTime(expiration)
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .keyID(credentials.clientKeyId())
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(signer);

            logger.debug("JWT token generated with jti={}", jti);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new SettingsClientException("Failed to generate JWT token", e);
        }
    }
}
