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

import com.cloudbees.api.ApplicationScaleResponse;
import com.cloudbees.api.BeesClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Scale an application up or down")
@CLICommand("app:scale")
public class ApplicationScale extends ApplicationBase {
    private String up;

    private String down;

    public ApplicationScale() {
        setArgumentExpected(0);
    }

    public void setUp(String up) {
        this.up = up;
    }

    public String getUp() {
        return up;
    }

    public String getDown() {
        return down;
    }

    public void setDown(String down) {
        this.down = down;
    }

    @Override
    protected boolean preParseCommandLine() {
        if(super.preParseCommandLine()) {
            addOption( "up", true, "scale up by");
            addOption( "down", true, "scale down by" );

            return true;
        }
        return false;
    }

    @Override
    protected boolean postParseCommandLine() {
        if (super.postParseCommandLine()) {
            if (getUp() == null && getDown() == null)
                throw new IllegalArgumentException("Either up or down option needs to be specified");
        }
        return true;
    }

    @Override
    protected boolean execute() throws Exception {
        String appid = getAppId();

        int quantity;
        if (getUp() != null)
            quantity = Integer.parseInt(getUp());
        else
            quantity = -Integer.parseInt(getDown());

        BeesClient client = getBeesClient(BeesClient.class);
        ApplicationScaleResponse res = client.applicationScale(appid, quantity);
        if (isTextOutput()) {
            System.out.println("application - " + appid + ": " + res.getStatus());
        } else
            printOutput(res, ApplicationScaleResponse.class);
        return true;
    }
}
