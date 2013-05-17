package com.cloudbees.sdk.commands.app;

import com.cloudbees.api.config.ParameterSettings;
import com.cloudbees.api.config.ResourceSettings;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabian Donze
 */
@XStreamAlias("appserver")
public class AppServerXML {

    @XStreamImplicit(itemFieldName="resource")
    private List<ResourceSettings> resources;


    public AppServerXML() {
    }

    public List<ResourceSettings> getResources() {
        if (resources == null)
            resources = new ArrayList<ResourceSettings>();
        return resources;
    }

    public void setResources(List<ResourceSettings> resources) {
        this.resources = resources;
    }

    private static XStream createXStream() {
        XStream xstream = new XStream();
        xstream.processAnnotations(AppServerXML.class);
        xstream.processAnnotations(ParameterSettings.class);
        xstream.processAnnotations(ResourceSettings.class);
        return xstream;
    }

    public static void toXML(AppServerXML appServerXML, FileOutputStream fos) {
        XStream xstream = createXStream();
        xstream.toXML(appServerXML, fos);
    }

    @Override
    public String toString() {
        return "AppServerXML{" +
                ", resources=" + resources +
                '}';
    }
}
