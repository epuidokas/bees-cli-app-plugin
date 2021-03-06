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

import com.cloudbees.api.ServiceResourceInfo;
import com.cloudbees.api.ServiceResourceListResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
@BeesCommand(group="Application", description = "List application resources")
@CLICommand("app:resource:list")
public class ApplicationResourceList extends ApplicationResourceBase {
    private String type;

    public ApplicationResourceList() {
        super();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "t", "type", true, "resource type" );
        }
        return true;
    }

    @Override
    protected boolean execute() throws Exception {
        AppClient client = getBeesClient(AppClient.class);
        ServiceResourceListResponse res = client.serviceResourceList(getServiceName(), getAccount(), getType());
        List<ServiceResourceInfo> resources = new ArrayList<ServiceResourceInfo>();
        for (ServiceResourceInfo resource: res.getResources()) {
            if (resource.getResourceType() != null && !resource.getResourceType().equalsIgnoreCase("application")) {
                if (getType() == null || resource.getResourceType().equalsIgnoreCase(getType()))
                    resources.add(resource);
            }
        }
        res.setResources(resources);
        displayResult(res);
        return true;
    }

    protected void displayResult(ServiceResourceListResponse res) {
        if (isTextOutput()) {
            List<ServiceResourceInfo> resources = res.getResources();
            System.out.println("Resources:");
            for (ServiceResourceInfo resource: resources) {
                if (resource.getResourceType() != null)
                    System.out.println(resource.getResourceType() + " " + resource.getService() + ":" + resource.getId());
                else
                    System.out.println(resource.getService() + ":" + resource.getId());
                Map<String, String> config = resource.getConfig();
                if(config != null && config.size() > 0) {
                    System.out.println("  config:");
                    for (Map.Entry<String, String> entry : config.entrySet()) {
                        System.out.println("    " + entry.getKey() + "=" + entry.getValue());
                    }
                }
                Map<String, String> settings = resource.getSettings();
                if(settings != null && settings.size() > 0) {
                    System.out.println("  settings:");
                    for (Map.Entry<String, String> entry : settings.entrySet()) {
                        System.out.println("    " + entry.getKey() + "=" + entry.getValue());
                    }
                }
            }
        } else
            printOutput(res, ServiceResourceListResponse.class, ServiceResourceInfo.class);
    }

}
