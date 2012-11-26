package com.cloudbees.sdk.commands.app;

import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Tail application instance log")
@CLICommand("app:instance:tail")
public class ApplicationInstanceTail extends ApplicationInstanceBase {

    public ApplicationInstanceTail() {
        super();
    }

    @Override
    protected boolean execute() throws Exception {
        String instanceId = getInstanceId();

        AppClient client = getBeesClient(AppClient.class);
        client.applicationInstanceTailLog(instanceId, "server", System.out);

        return true;
    }

}
