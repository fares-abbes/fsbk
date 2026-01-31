package com.mss.backOffice.request;

import java.util.ArrayList;
import java.util.List;


public class Nav {
    private String name;
    private String url;
    private String icon;
    private String permission;
    private List<Child> children = new ArrayList<Child>();




    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public String getPermission() {
        return permission;
    }
    public void setPermission(String permission) {
        this.permission = permission;
    }
    public List<Child> getChildren() {
        return children;
    }
    public void setChildren(List<Child> children) {
        this.children = children;
    }
}
