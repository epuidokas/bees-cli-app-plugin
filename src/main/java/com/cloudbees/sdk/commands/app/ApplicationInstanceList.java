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
        AppClient client = getBeesClient(AppClient.class);
        ApplicationInstanceListResponse res = client.applicationInstanceList(getAppId());
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
