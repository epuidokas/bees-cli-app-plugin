package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationRestartResponse;
import com.cloudbees.api.BeesClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Restart/redeploy an application")
@CLICommand("app:restart")
public class ApplicationRestart extends ApplicationBase {
    private Boolean force;

    public ApplicationRestart() {
        setArgumentExpected(0);
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    @Override
    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "f", "force", false, "force restart without prompting" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String appid = getAppId();

        if (force == null || !force.booleanValue()) {
            if (!Helper.promptMatches("Are you sure you want to restart this application [" + appid + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        BeesClient client = getBeesClient();
        ApplicationRestartResponse res = client.applicationRestart(appid);

        if (isTextOutput()) {
            if(res.isRestarted())
                System.out.println("application restarted - " + appid);
            else
                System.out.println("application could not be restarted");
        } else
            printOutput(res, ApplicationRestartResponse.class);

        return true;
    }

}
