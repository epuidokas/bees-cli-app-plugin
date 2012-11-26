package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationInstanceStatusResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Restart an application instance")
@CLICommand("app:instance:restart")
public class ApplicationInstanceRestart extends ApplicationInstanceBase {
    private Boolean force;

    public ApplicationInstanceRestart() {
        super();
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "f", "force", false, "force restart without prompting" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String instanceId = getInstanceId();

        if (force == null || !force.booleanValue()) {
            if (!Helper.promptMatches("Are you sure you want to restart this instance [" + instanceId + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        AppClient client = getBeesClient(AppClient.class);
        ApplicationInstanceStatusResponse res = client.applicationInstanceRestart(instanceId);

        if (isTextOutput()) {
            System.out.println(String.format("instance [%s]: %s",instanceId, res.getStatus()));
        } else
            printOutput(res, ApplicationInstanceStatusResponse.class);

        return true;
    }

}
