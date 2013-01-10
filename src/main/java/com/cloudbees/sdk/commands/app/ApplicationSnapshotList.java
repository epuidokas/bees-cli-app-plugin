package com.cloudbees.sdk.commands.app;


import com.cloudbees.api.ApplicationSnapshotInfo;
import com.cloudbees.api.ApplicationSnapshotListResponse;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;

/**
 * @author Fabian Donze
 */
@BeesCommand(group="Application", description = "List application snapshots")
@CLICommand("app:snapshot:list")
public class ApplicationSnapshotList extends ApplicationBase {

    public ApplicationSnapshotList() {
    }

    @Override
    protected boolean execute() throws Exception {
        AppClient client = getBeesClient(AppClient.class);
        ApplicationSnapshotListResponse res = client.applicationSnapshotList(getAppId());
        if (isTextOutput()) {
            for (ApplicationSnapshotInfo snapshotInfo : res.getSnapshots()) {
                ApplicationSnapshotBase.printApplicationSnapshotInfo(snapshotInfo);
                System.out.println();
            }
        } else {
            printOutput(res, ApplicationSnapshotInfo.class, ApplicationSnapshotListResponse.class);
        }

        return true;
    }

}
