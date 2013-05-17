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

import com.cloudbees.api.ServiceResourceDeleteResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Delete an application resource")
@CLICommand("app:resource:delete")
public class ApplicationResourceDelete extends ApplicationResourceBase {
    private Boolean force;

    public ApplicationResourceDelete() {
        setArgumentExpected(1);
    }

    protected boolean forceDelete() {
        return force == null ? false : force.booleanValue();
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    @Override
    protected String getUsageMessage() {
        return "RESOURCE_NAME";
    }

    @Override
    protected boolean preParseCommandLine() {
        if(super.preParseCommandLine()) {
            addOption( "f", "force", false, "force delete without prompting" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String resource = getParameterName();
        String[] parts = resource.split("/");
        if (parts.length == 1)
            resource = getAccount() + "/" + resource;
        if (!forceDelete()) {
            if (!Helper.promptMatches("Are you sure you want to delete this application resource [" + resource + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        AppClient client = getBeesClient(AppClient.class);
        ServiceResourceDeleteResponse res = client.serviceResourceDelete(getServiceName(), resource);
        if (isTextOutput()) {
            if(res.isDeleted()) {
                System.out.println(String.format("Application resource %s deleted.", resource));
            } else {
                System.out.println("Application resource could not be deleted");
            }
        } else
            printOutput(res, ServiceResourceDeleteResponse.class);

        return true;
    }

}

