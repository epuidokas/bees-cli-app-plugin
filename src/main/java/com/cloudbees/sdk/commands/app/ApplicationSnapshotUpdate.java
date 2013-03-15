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

import com.cloudbees.api.ApplicationSnapshotStatusResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Update application snapshot information")
@CLICommand("app:snapshot:update")
public class ApplicationSnapshotUpdate extends ApplicationSnapshotBase {
    private String title;

    public void setTitle(String title) {
        this.title = title;
    }

    public ApplicationSnapshotUpdate() {
        super();
    }

    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "t", "title", true, "The snapshot title" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String snapshotId = getSnapshotId();

        AppClient client = getBeesClient(AppClient.class);
        Map<String, String> parameters = new HashMap<String, String>();
        if (title != null)
            parameters.put("title", title);
        ApplicationSnapshotStatusResponse res = client.applicationSnapshotUpdate(snapshotId, parameters);

        if (isTextOutput()) {
            System.out.println(String.format("snapshot [%s]: %s", snapshotId, res.getStatus()));
        } else
            printOutput(res, ApplicationSnapshotStatusResponse.class);

        return true;
    }

}
