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

import com.cloudbees.api.ApplicationStatusResponse;
import com.cloudbees.api.BeesClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Stop an application")
@CLICommand("app:stop")
public class ApplicationStop extends ApplicationBase {
    private Boolean force;

    public ApplicationStop() {
        setArgumentExpected(0);
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "f", "force", false, "force stop without prompting" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String appid = getAppId();

        String defaultAppDomain = getConfigProperties().getProperty("bees.project.app.domain");
        String[] appIdParts = appid.split("/");
        if (appIdParts.length < 2) {
            if (defaultAppDomain != null && !defaultAppDomain.equals("")) {
                appid = defaultAppDomain + "/" + appid;
            } else {
                throw new RuntimeException("default app account could not be determined, appid needs to be fully-qualified ");
            }
        }

        if (force == null || !force.booleanValue()) {
            if (!Helper.promptMatches("Are you sure you want to stop this application [" + appid + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        AppClient client = getAppClient(appid);
        ApplicationStatusResponse res = client.applicationStop(appid);

        if (isTextOutput()) {
            if(res.getStatus().equalsIgnoreCase("stopped"))
                System.out.println("application stopped - " + appid);
            else
                System.out.println("application could not be stopped, current status: " + res.getStatus());
        } else
            printOutput(res, ApplicationStatusResponse.class);

        return true;
    }

}
