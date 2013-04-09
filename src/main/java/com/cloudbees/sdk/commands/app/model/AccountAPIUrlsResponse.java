package com.cloudbees.sdk.commands.app.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.Map;

/**
 * @author Fabian Donze
 */
@XStreamAlias("AccountAPIUrlsResponse")
public class AccountAPIUrlsResponse {
    String account;
    Map<String, String> urls;

    public AccountAPIUrlsResponse(String account) {
        this.account = account;
    }

    public String getAccount() {
        return account;
    }

    public Map<String, String> getUrls() {
        return urls;
    }

    public void setUrls(Map<String, String> urls) {
        this.urls = urls;
    }
}