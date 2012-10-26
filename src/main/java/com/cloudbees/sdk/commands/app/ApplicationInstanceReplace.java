package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationInstanceStatusResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Re-deploy a specific application instance")
@CLICommand("app:instance:replace")
public class ApplicationInstanceReplace extends ApplicationInstanceBase {
    private Boolean force;

    public ApplicationInstanceReplace() {
        super();
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "f", "force", false, "force replace without prompting" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String instanceId = getInstanceId();

        if (force == null || !force.booleanValue()) {
            if (!Helper.promptMatches("Are you sure you want to replace this instance [" + instanceId + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        AppClient client = getStaxClient(AppClient.class);
        ApplicationInstanceStatusResponse res = client.applicationInstanceReplace(instanceId);

        if (isTextOutput()) {
            System.out.println(String.format("instance [%s]: %s",instanceId, res.getStatus()));
        } else
            printOutput(res, ApplicationInstanceStatusResponse.class);

        return true;
    }

}
