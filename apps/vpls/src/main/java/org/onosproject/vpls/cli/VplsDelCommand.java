/*
 * Copyright 2016-present Open Networking Laboratory
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

package org.onosproject.vpls.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.vpls.config.VplsConfigurationService;

/**
 * CLI to remove VPLSs.
 */
@Command(scope = "onos", name = "vpls-del",
        description = "Deletes an existing VPLS")
public class VplsDelCommand extends AbstractShellCommand {

    private VplsConfigurationService vplsConfigService =
            get(VplsConfigurationService.class);

    @Argument(index = 0, name = "vplsName", description = "Name of the VPLS",
            required = true, multiValued = false)
    private String vplsName = null;

    @Override
    protected void execute() {
        if (!VplsCommandUtils.vplsExists(vplsName)) {
            print(VplsCommandUtils.VPLS_NOT_FOUND, vplsName);
            return;
        }

        vplsConfigService.removeVpls(vplsName);
    }
}
