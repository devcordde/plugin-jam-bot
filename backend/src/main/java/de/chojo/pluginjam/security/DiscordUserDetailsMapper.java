package de.chojo.pluginjam.security;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.oauth2.endpoint.authorization.state.State;
import io.micronaut.security.oauth2.endpoint.token.response.OauthAuthenticationMapper;
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Named("discord")
@Singleton
public class DiscordUserDetailsMapper implements OauthAuthenticationMapper {

    private final HttpClient httpClient;

    public DiscordUserDetailsMapper(@Client("https://discord.com/api") HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Publisher<AuthenticationResponse> createAuthenticationResponse(TokenResponse tokenResponse, State state) {
        MutableHttpRequest<?> request = HttpRequest.GET("/users/@me")
                .bearerAuth(tokenResponse.getAccessToken());

        return Mono.from(httpClient.retrieve(request, Map.class))
                .map(userMap -> {
                    String username = (String) userMap.get("username");
                    String globalName = (String) userMap.get("global_name");
                    String id = (String) userMap.get("id");
                    String avatar = (String) userMap.get("avatar");
                    String avatarUrl = avatar == null
                            ? "https://cdn.discordapp.com/embed/avatars/" + (Long.parseLong(id) >> 22) % 6 + ".png"
                            : "https://cdn.discordapp.com/avatars/" + id + "/" + avatar + ".png";

                    return AuthenticationResponse.success(id,
                            List.of("ROLE_USER"),
                            Map.of("username", username,
                                    "name", globalName != null ? globalName : username,
                                    "picture", avatarUrl));
                });
    }
}