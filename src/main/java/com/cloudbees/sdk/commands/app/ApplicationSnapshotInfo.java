package com.cloudbees.sdk.commands.app;

import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "Get application snapshot information")
@CLICommand("app:snapshot:info")
public class ApplicationSnapshotInfo extends ApplicationSnapshotBase {

    public ApplicationSnapshotInfo() {
        super();
    }

    @Override
    protected boolean execute() throws Exception {
        String snapshotId = getSnapshotId();

        AppClient client = getBeesClient(AppClient.class);
        com.cloudbees.api.ApplicationSnapshotInfo snapshotInfo = client.applicationSnapshotInfo(snapshotId);

        if (isTextOutput()) {
            ApplicationSnapshotBase.printApplicationSnapshotInfo(snapshotInfo);
        } else
            printOutput(snapshotInfo, com.cloudbees.api.ApplicationSnapshotInfo.class);

        return true;
    }

}
