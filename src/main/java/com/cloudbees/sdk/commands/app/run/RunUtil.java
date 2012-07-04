package com.cloudbees.sdk.commands.app.run;

import com.staxnet.appserver.IAppServerConfiguration;
import com.staxnet.appserver.StaxSdkAppServer;
import com.staxnet.appserver.WarBasedServerConfiguration;
import com.staxnet.appserver.config.ResourceConfig;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabian Donze
 */
public class RunUtil extends Thread {
    File appserverXML;
    String[] environments;
    String tmpDir;
    File webRoot;
    Integer port;
    File staxWebXml;
    StaxSdkAppServer server;

    public RunUtil(File appserverXML, String[] environments, String tmpDir, File webRoot, Integer port, File staxWebXml) {
        super();
        this.appserverXML = appserverXML;
        this.environments = environments;
        this.tmpDir = tmpDir;
        this.webRoot = webRoot;
        this.port = port;
        this.staxWebXml = staxWebXml;
    }

    @Override
    public void start() {
        super.start();
        try {
            IAppServerConfiguration config = WarBasedServerConfiguration.load(appserverXML, webRoot, staxWebXml, environments);

            if (staxWebXml.exists()) {
                // Resolve variables
                String appxml = readFile(staxWebXml);
                for (Map.Entry<String, String> entry: getSystemProperties(config).entrySet()) {
                    appxml = appxml.replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue());
                }
                saveFile(staxWebXml, appxml);

                if (appserverXML.exists()) {
                    appxml = readFile(appserverXML);
                    for (Map.Entry<String, String> entry: getSystemProperties(config).entrySet()) {
                        appxml = appxml.replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue());
                    }
                    saveFile(appserverXML, appxml);
                }

                config = WarBasedServerConfiguration.load(appserverXML, webRoot, staxWebXml, environments);
            }


            getWorkDir().mkdirs();
            server = new StaxSdkAppServer(new File(getTmpDir()).getAbsolutePath(),
                    getWorkDir().getAbsolutePath(),
                    getClass().getClassLoader(), new String[0],
                    port, config, null);

            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if (server != null) server.stop();
    }

    private void saveFile(File staxWebXml, String appxml) throws IOException {
        FileWriter fos = null;
        try {
            fos = new FileWriter(staxWebXml);
            fos.write(appxml);
        } finally {
            if (fos != null)
                fos.close();
        }
    }


    private String readFile(File file) throws IOException {
        FileReader fr = null;
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            StringBuffer result = new StringBuffer();
            while ((line = br.readLine()) != null) {
                result.append(line);
            }

            return result.toString();
        } finally {
            if (fr != null) fr.close();
        }
    }

    private File getWorkDir() {
        return new File(getTmpDir(), "workdir");
    }

    private String getTmpDir() {
        return tmpDir == null ? "tmp" : tmpDir;
    }

    private Map<String, String> getSystemProperties(IAppServerConfiguration config) {
        Map<String, String> systemProperties = new HashMap<String, String>();

        for (ResourceConfig rs : config.getServerResources()) {
            String type = rs.getType();
            if (type == null || type.equalsIgnoreCase("system-property")) {
                systemProperties.put(rs.getName(), rs.getValue());
            }
        }
        for (ResourceConfig rs : config.getAppConfiguration().getResources()) {
            String type = rs.getType();
            if (type == null || type.equalsIgnoreCase("system-property")) {
                systemProperties.put(rs.getName(), rs.getValue());
            }
        }
        return systemProperties;
    }

}
