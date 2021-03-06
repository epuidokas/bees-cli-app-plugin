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


import com.cloudbees.api.ApplicationInstanceInfo;
import com.cloudbees.api.ApplicationInstanceListResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "List application instances")
@CLICommand("app:instance:list")
public class ApplicationInstanceList extends ApplicationBase {

    public ApplicationInstanceList() {
    }

    @Override
    protected boolean execute() throws Exception {
        String appid = getAppId();
        AppClient client = getAppClient(appid);
        ApplicationInstanceListResponse res = client.applicationInstanceList(appid);
        if (isTextOutput()) {
            for (ApplicationInstanceInfo instanceInfo : res.getInstances()) {
                ApplicationInstanceBase.printApplicationInstanceInfo(instanceInfo);
                System.out.println();
            }
        } else {
            printOutput(res, ApplicationInstanceInfo.class, ApplicationInstanceListResponse.class);
        }

        return true;
    }

}
