package de.chojo.pluginjam.security;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.filters.AuthenticationFetcher;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.List;

@Singleton
public class BotApiKeyAuthenticationFetcher implements AuthenticationFetcher<HttpRequest<?>> {

    private final String configuredApiKey;

    public BotApiKeyAuthenticationFetcher(@Value("${bot.api-key}") String configuredApiKey) {
        this.configuredApiKey = configuredApiKey;
    }

    @Override
    public Publisher<Authentication> fetchAuthentication(HttpRequest<?> request) {
        String requestApiKey = request.getHeaders().get("X-API-Key");

        if (requestApiKey != null && requestApiKey.equals(configuredApiKey)) {
            Authentication authentication = Authentication.build("discord-bot", List.of("ROLE_BOT"));
            
            return Mono.just(authentication);
        }

        return Mono.empty();
    }
}