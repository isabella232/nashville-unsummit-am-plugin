/*
 *    Copyright 2017-2019 ForgeRock AS.
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

import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;

import java.util.Optional;

import javax.inject.Inject;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.TextOutputCallback;

import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.SingleOutcomeNode;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.realms.Realm;
import org.forgerock.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;

/**
 * A node that checks to see if zero-page login headers have specified username and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider  = SingleOutcomeNode.OutcomeProvider.class,
               configClass      = NashvilleNode.Config.class)
public class NashvilleNode extends SingleOutcomeNode {

    private final Logger logger = LoggerFactory.getLogger(NashvilleNode.class);
    private final Greetings greetings;
    private final Realm realm;

    /**
     * Configuration for the node.
     */
    public interface Config {
    }


    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     */
    @Inject
    public NashvilleNode(@Assisted Realm realm, Greetings greetings) {
        this.realm = realm;
        this.greetings = greetings;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        Optional<ConfirmationCallback> callback = context.getCallback(ConfirmationCallback.class);
        if (callback.isPresent() && callback.get().getSelectedIndex() == 0) {
            return goToNext().build();
        }
        String username = context.sharedState.get(USERNAME).asString();
        if (Strings.isNullOrEmpty(username)) {
            throw new NodeProcessException("Node needs to be used after a username has been identified");
        }
        Optional<String> message = greetings.getGreeting(realm, username);
        return message.map(s -> Action.send(
                new TextOutputCallback(TextOutputCallback.INFORMATION, s),
                new ConfirmationCallback(ConfirmationCallback.INFORMATION, new String[]{"Ok"}, 0)
        )).orElseGet(this::goToNext).build();
    }
}
