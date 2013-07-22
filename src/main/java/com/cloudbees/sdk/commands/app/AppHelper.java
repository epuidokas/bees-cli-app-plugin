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

import com.cloudbees.api.AccountRegionInfo;
import com.cloudbees.api.AccountRegionListResponse;
import com.cloudbees.api.BeesClientConfiguration;
import com.cloudbees.api.ServiceResourceInfo;
import com.cloudbees.sdk.utils.Helper;
import com.staxnet.appserver.config.AppConfig;
import com.staxnet.appserver.config.AppConfigHelper;
import com.staxnet.appserver.utils.ZipHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;

/**
 * @author Kohsuke Kawaguchi
 */
public class AppHelper {
    public static String getArchiveApplicationId() {
        String appid = null;
        File deployFile = new File("build/webapp.war");
        if (!deployFile.exists()) {
            File dir = new File("target");
            String[] files = Helper.getFiles(dir, ".war");
            if (files != null && files.length == 1) {
                deployFile = new File(dir, files[0]);
            }
        }
        if (deployFile.exists()) {
            AppConfig appConfig = null;
            try {
                appConfig = getAppConfig(deployFile, new String[0], new String[] { "deploy" });
            } catch (IOException e) {
                System.err.println("WARNING: " + e.getMessage());
            }
            appid = appConfig.getApplicationId();
        }
        return appid;
    }

    public static AppConfig getAppConfig(File deployZip, final String[] environments,
            final String[] implicitEnvironments) throws IOException {
        final AppConfig appConfig = new AppConfig();

        FileInputStream fin = new FileInputStream(deployZip);
        try {
            ZipHelper.unzipFile(fin, new ZipHelper.ZipEntryHandler() {
                public void unzip(ZipEntry entry, InputStream zis)
                        throws IOException {
                    if (entry.getName().equals("META-INF/stax-application.xml")
                            || entry.getName().equals("WEB-INF/stax-web.xml")
                            || entry.getName().equals("WEB-INF/cloudbees-web.xml")) {
                        AppConfigHelper.load(appConfig, zis, null, environments, implicitEnvironments);
                    }
                }
            }, false);
        } finally {
            fin.close();
        }

        return appConfig;
    }

    public static AccountRegionInfo getApplicationRegionInfo(AppClient client, String appId) {
        try {
            ServiceResourceInfo resourceInfo = client.serviceResourceInfo("cb-app", appId);
            Map<String, String> config= resourceInfo.getConfig();
            if (config != null) {
                String region = config.get("region");
                if (region != null) {
                    String parts[] = appId.split("/");
                    AccountRegionListResponse res = client.accountRegionList(parts[0], null);
                    return getRegionInfo(res, region);
                }
            }
        } catch (Exception e) {
            if (!e.getMessage().toLowerCase().contains("no such resource"))
                System.err.println("Error: " + e.getMessage());
        }

        return null;
    }

    private static AccountRegionInfo getRegionInfo(AccountRegionListResponse regions, String region) {
        for (AccountRegionInfo regionInfo : regions.getRegions()) {
            if (regionInfo.getName().equalsIgnoreCase(region)) {
                return regionInfo;
            }
        }
        return null;
    }
}
