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


import com.cloudbees.api.BeesClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Tail the logs of an application")
@CLICommand("app:tail")
public class ApplicationTail extends ApplicationBase {

    private String logname;


    public ApplicationTail() {
        setArgumentExpected(0);
    }

    public void setLogname(String logname) {
        this.logname = logname;
    }

    @Override
    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "l", "logname", true, "The log name: server, access or error (Default: 'server')" );
            removeOption("o");
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        if (logname == null) logname = "server";

        BeesClient client = getBeesClient(BeesClient.class);
        client.tailLog(getAppId(), logname, System.out);

        return true;
    }

}
