package com.cloudbees.sdk.commands.app;

import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Get application instance information")
@CLICommand("app:instance:info")
public class ApplicationInstanceInfo extends ApplicationInstanceBase {

    public ApplicationInstanceInfo() {
        super();
    }

    @Override
    protected boolean execute() throws Exception {
        String instanceId = getInstanceId();

        AppClient client = getStaxClient(AppClient.class);
        com.cloudbees.api.ApplicationInstanceInfo instanceInfo = client.applicationInstanceInfo(instanceId);

        if (isTextOutput()) {
            ApplicationInstanceBase.printApplicationInstanceInfo(instanceInfo);
        } else
            printOutput(instanceInfo, com.cloudbees.api.ApplicationInstanceInfo.class);

        return true;
    }

}
