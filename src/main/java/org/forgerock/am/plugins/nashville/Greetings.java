/*
 *    Copyright 2019 ForgeRock AS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.forgerock.am.plugins.nashville;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.forgerock.http.Client;
import org.forgerock.http.protocol.Request;
import org.forgerock.openam.core.realms.Realm;
import org.forgerock.openam.sm.AnnotatedServiceRegistry;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.iplanet.sso.SSOException;
import com.sun.identity.sm.SMSException;

@Singleton
public class Greetings {

    private final LoadingCache<Realm, RealmGreetingService> realmServices;
    private final Client httpClient;
    private final AnnotatedServiceRegistry serviceRegistry;

    @Inject
    public Greetings(Client httpClient, AnnotatedServiceRegistry serviceRegistry) {
        this.httpClient = httpClient;
        this.serviceRegistry = serviceRegistry;
        realmServices = CacheBuilder.newBuilder()
                .build(CacheLoader.from(this::init));
    }

    private RealmGreetingService init(Realm realm) {
        try {
            String serviceUrl = serviceRegistry.getRealmSingleton(GreetingsService.class, realm).get().serviceUrl();
            return new RealmGreetingService(httpClient, serviceUrl);
        } catch (SMSException | SSOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<String> getGreeting(Realm realm, String username) {
        return realmServices.getUnchecked(realm).greetingsCache.getUnchecked(username);
    }

    private static class RealmGreetingService {
        private final LoadingCache<String, Optional<String>> greetingsCache;
        private final Client httpClient;
        private final String serviceUrl;

        RealmGreetingService(Client httpClient, String serviceUrl) {
            this.httpClient = httpClient;
            this.serviceUrl = serviceUrl;
            greetingsCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(Duration.ofHours(1))
                    .build(CacheLoader.from(this::loadGreeting));
        }

        private Optional<String> loadGreeting(String username) {
            String uri = serviceUrl + "?username=" + username;
            try {
                byte[] message = httpClient.send(new Request().setMethod("GET").setUri(uri))
                        .getOrThrow()
                        .getEntity()
                        .getBytes();
                if (message.length == 0) {
                    return Optional.empty();
                }
                return Optional.of(new String(message, UTF_8));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted");
            }
        }
    }
}
