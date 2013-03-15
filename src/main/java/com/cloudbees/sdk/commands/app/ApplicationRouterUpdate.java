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
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

import java.io.File;
import java.util.Map;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Update a router resource configuration")
@CLICommand("app:router:update")
public class ApplicationRouterUpdate extends ApplicationResourceBase {
    private String certificate;
    private String privateKey;
    private Boolean ssl;

    public ApplicationRouterUpdate() {
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    @Override
    protected boolean preParseCommandLine() {
        if(super.preParseCommandLine()) {
            addOption( "cert", "certificate", true, "SSL certificate file" );
            addOption( "pk", "privateKey", true, "SSL private key file" );
            addOption( "ssl", false, "SSL router" );

            return true;
        }
        return false;
    }

    @Override
    protected String getUsageMessage() {
        return "ROUTER_RESOURCE_NAME";
    }

    @Override
    protected boolean execute() throws Exception {
        Map<String, String> settings = getSettings();
        if (getCertificate() != null) {
            settings.put("cert", "file://" + getCertificate());
            if (!new File(getCertificate()).exists()) {
                throw new IllegalArgumentException("Certificate file not found: " + getCertificate());
            }
        }
        if (getPrivateKey() != null) {
            settings.put("key", "file://" + getPrivateKey());
            if (!new File(getPrivateKey()).exists()) {
                throw new IllegalArgumentException("Private key file not found: " + getPrivateKey());
            }
        }
        if (getSsl() != null)
            settings.put("ssl", getSsl().toString());

        String resourceId = getParameterName();
        String[] parts = resourceId.split("/");
        if (parts.length == 1)
            resourceId = getAccount() + "/" + resourceId;
        StaxClient client = getBeesClient(StaxClient.class);
        ServiceResourceInfo resource = client.serviceResourceUpdate(getServiceName(), resourceId, getSettings());
        if (isTextOutput()) {
            System.out.println("Resource: " + resource.getId());
            Map<String, String> config = resource.getConfig();
            if(config != null && config.size() > 0) {
                System.out.println("config:");
                for (Map.Entry<String, String> entry : config.entrySet()) {
                    System.out.println("  " + entry.getKey() + "=" + entry.getValue());
                }
            }
            settings = resource.getSettings();
            if(settings != null && settings.size() > 0) {
                System.out.println("settings:");
                for (Map.Entry<String, String> entry : settings.entrySet()) {
                    System.out.println("  " + entry.getKey() + "=" + entry.getValue());
                }
            }
        } else
            printOutput(resource, ServiceResourceInfo.class);

        return true;
    }
}
