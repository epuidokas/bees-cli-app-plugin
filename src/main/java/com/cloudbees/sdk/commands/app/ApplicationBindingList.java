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

import com.cloudbees.api.ResourceBindingInfo;
import com.cloudbees.api.ServiceResourceBindingListResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

import java.util.Map;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "List resources bound to an application")
@CLICommand("app:bindings")
public class ApplicationBindingList extends ApplicationBase {

    public ApplicationBindingList() {
        setArgumentExpected(0);
    }

    @Override
    protected boolean execute() throws Exception {
        AppClient client = getBeesClient(AppClient.class);
        ServiceResourceBindingListResponse res = client.resourceBindingList("cb-app", getAppId());
        if (isTextOutput()) {
            System.out.println("Applications bindings:");
            for (ResourceBindingInfo binding: res.getBindings()) {
                System.out.println(binding.getAlias() + " " + binding.getToService() + ":" + binding.getToResourceId());
                Map<String, String> config = binding.getConfig();
                if(config != null && config.size() > 0) {
                    System.out.println("  config:");
                    for (Map.Entry<String, String> entry : config.entrySet()) {
                        System.out.println("    " + entry.getKey() + "=" + entry.getValue());
                    }
                }
            }
        } else
            printOutput(res, ResourceBindingInfo.class, ServiceResourceBindingListResponse.class);
        return true;
    }

}
