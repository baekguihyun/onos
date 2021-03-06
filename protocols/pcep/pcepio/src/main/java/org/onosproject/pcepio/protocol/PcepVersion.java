/*
 * Copyright 2015-present Open Networking Foundation
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

package org.onosproject.pcepio.protocol;

/**
 * Enum to provide PCEP Message Version.
 */
public enum PcepVersion {

    PCEP_1(1);

    public final int packetVersion;

    /**
     * Assign PCEP PacketVersion with WireVersion.
     *
     * @param wireVersion version of pcep
     */
    PcepVersion(final int wireVersion) {

        this.packetVersion = wireVersion;
    }

    /**
     * Returns Wire version of PCEP Message.
     *
     * @return packetVersion
     */
    public int getWireVersion() {
        return packetVersion;
    }
}
