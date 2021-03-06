/*
 * Copyright 2010-2013, CloudBees Inc.
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

package com.cloudbees.sdk.commands.app;

import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Get application instance information")
@CLICommand("app:instance:info")
public class ApplicationInstanceInfo extends ApplicationInstanceBase {

    public ApplicationInstanceInfo() {
        super();
    }

    @Override
    protected boolean execute() throws Exception {
        String instanceId = getInstanceId();

        AppClient client = getBeesClient(AppClient.class);
        com.cloudbees.api.ApplicationInstanceInfo instanceInfo = client.applicationInstanceInfo(instanceId);

        if (isTextOutput()) {
            ApplicationInstanceBase.printApplicationInstanceInfo(instanceInfo);
        } else
            printOutput(instanceInfo, com.cloudbees.api.ApplicationInstanceInfo.class);

        return true;
    }

}
