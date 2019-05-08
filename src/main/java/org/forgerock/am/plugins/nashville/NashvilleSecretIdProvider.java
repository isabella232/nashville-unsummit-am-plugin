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

import static org.forgerock.secrets.Purpose.purpose;

import org.forgerock.openam.secrets.SecretIdProvider;
import org.forgerock.secrets.GenericSecret;
import org.forgerock.secrets.Purpose;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

public class NashvilleSecretIdProvider implements SecretIdProvider {

    private static final String GREETINGS_API_KEY_ID = "am.plugins.nashville.greetings.api";
    static final Purpose<GenericSecret> GREETINGS_API_KEY = purpose(GREETINGS_API_KEY_ID, GenericSecret.class);

    @Override
    public Multimap<String, String> getRealmSingletonSecretIds() {
        return ImmutableMultimap.of("nashville-plugin", GREETINGS_API_KEY_ID);
    }
}
