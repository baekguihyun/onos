/*
 * Copyright 2018-present Open Networking Foundation
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
package org.onosproject.store.atomix.primitives.impl;

import io.atomix.core.Atomix;
import io.atomix.primitive.Recovery;
import io.atomix.protocols.raft.MultiRaftProtocol;
import org.onosproject.store.service.Topic;
import org.onosproject.store.service.TopicBuilder;

/**
 * Default topic builder.
 */
public class AtomixDistributedTopicBuilder<T> extends TopicBuilder<T> {
    private static final int MAX_RETRIES = 5;
    private final Atomix atomix;
    private final String group;

    public AtomixDistributedTopicBuilder(Atomix atomix, String group) {
        this.atomix = atomix;
        this.group = group;
    }

    @Override
    public Topic<T> build() {
        return new AtomixDistributedTopic<>(atomix.<T>atomicValueBuilder(name())
            .withProtocol(MultiRaftProtocol.builder(group)
                .withRecoveryStrategy(Recovery.RECOVER)
                .withMaxRetries(MAX_RETRIES)
                .build())
            .withReadOnly(readOnly())
            .withSerializer(new AtomixSerializerAdapter(serializer()))
            .build()
            .async());
    }
}
