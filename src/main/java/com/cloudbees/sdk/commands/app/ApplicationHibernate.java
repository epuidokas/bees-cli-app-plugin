package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.ApplicationStatusResponse;
import com.cloudbees.api.BeesClient;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.utils.Helper;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Hibernate an application")
@CLICommand("app:hibernate")
public class ApplicationHibernate extends ApplicationBase {
    private Boolean force;

    public ApplicationHibernate() {
        setArgumentExpected(0);
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    protected boolean preParseCommandLine() {
        if (super.preParseCommandLine()) {
            addOption( "f", "force", false, "force hibernation without prompting" );
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        String appid = getAppId();

        if (force == null || !force.booleanValue()) {
            if (!Helper.promptMatches("Are you sure you want to hibernate this application [" + appid + "]: (y/n) ", "[yY].*")) {
                return true;
            }
        }

        AppClient client = getBeesClient(AppClient.class);
        ApplicationStatusResponse res = client.applicationHibernate(appid);

        if (isTextOutput()) {
            if(res.getStatus().equalsIgnoreCase("hibernate"))
                System.out.println("application hibernated - " + appid);
            else
                System.out.println("application could not be hibernated, current status: " + res.getStatus());
        } else
            printOutput(res, ApplicationStatusResponse.class);

        return true;
    }

}
