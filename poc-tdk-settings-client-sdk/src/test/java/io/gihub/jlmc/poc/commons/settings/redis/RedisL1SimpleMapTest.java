package io.gihub.jlmc.poc.commons.settings.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.StatefulRedisConnectionImpl;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.caching.CacheFrontend;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RedisL1SimpleMapTest {

    @Test
    void shouldInitializeAndGetValue() {
        String redisUrl = "redis://localhost:6379";

        RedisClient clientMock = mock(RedisClient.class);
        StatefulRedisConnectionImpl<String, String> connectionMock = mock(StatefulRedisConnectionImpl.class);

        try (MockedStatic<RedisClient> redisClientStatic = mockStatic(RedisClient.class)) {
            redisClientStatic.when(() -> RedisClient.create(anyString())).thenReturn(clientMock);
            when(clientMock.connect()).thenReturn(connectionMock);
            
            // This test just verifies that the constructor can run when dependencies are mocked.
            // We can't easily verify the ClientSideCaching call without more complex mocking.
            new RedisL1SimpleMap(redisUrl);
            
            verify(clientMock).connect();
        }
    }

    @Test
    void shouldCallFrontendOnGetValue() throws NoSuchFieldException, IllegalAccessException {
        String redisUrl = "redis://localhost:6379";
        RedisClient clientMock = mock(RedisClient.class);
        StatefulRedisConnectionImpl<String, String> connectionMock = mock(StatefulRedisConnectionImpl.class);

        try (MockedStatic<RedisClient> redisClientStatic = mockStatic(RedisClient.class)) {
            redisClientStatic.when(() -> RedisClient.create(anyString())).thenReturn(clientMock);
            when(clientMock.connect()).thenReturn(connectionMock);

            RedisL1SimpleMap victim = new RedisL1SimpleMap(redisUrl);

            CacheFrontend<String, String> frontendMock = mock(CacheFrontend.class);
            java.lang.reflect.Field field = RedisL1SimpleMap.class.getDeclaredField("frontend");
            field.setAccessible(true);
            field.set(victim, frontendMock);

            when(frontendMock.get("key1")).thenReturn("value1");

            String result = victim.getValue("key1");

            assertEquals("value1", result);
            verify(frontendMock).get("key1");
        }
    }
}
