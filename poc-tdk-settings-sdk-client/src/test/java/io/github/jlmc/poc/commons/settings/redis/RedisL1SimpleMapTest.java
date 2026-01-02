package io.github.jlmc.poc.commons.settings.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.StatefulRedisConnectionImpl;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.support.caching.CacheFrontend;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisL1SimpleMapTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldInitializeAndGetValue() {
        String redisUrl = "redis://localhost:6379";

        RedisClient clientMock = mock(RedisClient.class);
        StatefulRedisConnectionImpl<String, String> connectionMock = mock(StatefulRedisConnectionImpl.class);
        RedisCommands<String, String> commandsMock = mock(RedisCommands.class);

        try (MockedStatic<RedisClient> redisClientStatic = mockStatic(RedisClient.class)) {
            redisClientStatic.when(() -> RedisClient.create(anyString())).thenReturn(clientMock);
            when(clientMock.connect()).thenReturn(connectionMock);
            when(connectionMock.sync()).thenReturn(commandsMock);
            
            // This test just verifies that the constructor can run when dependencies are mocked.
            // We can't easily verify the ClientSideCaching call without more complex mocking.
            new RedisL1SimpleMap(redisUrl, "namespace");
            
            verify(clientMock).connect();
            verify(connectionMock).sync();
            verify(commandsMock).clientTracking(any());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCallFrontendOnGetValue() throws NoSuchFieldException, IllegalAccessException {
        String redisUrl = "redis://localhost:6379";
        RedisClient clientMock = mock(RedisClient.class);
        StatefulRedisConnectionImpl<String, String> connectionMock = mock(StatefulRedisConnectionImpl.class);
        RedisCommands<String, String> commandsMock = mock(RedisCommands.class);

        try (MockedStatic<RedisClient> redisClientStatic = mockStatic(RedisClient.class)) {
            redisClientStatic.when(() -> RedisClient.create(anyString())).thenReturn(clientMock);
            when(clientMock.connect()).thenReturn(connectionMock);
            when(connectionMock.sync()).thenReturn(commandsMock);

            RedisL1SimpleMap victim = new RedisL1SimpleMap(redisUrl, "namespace");

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
