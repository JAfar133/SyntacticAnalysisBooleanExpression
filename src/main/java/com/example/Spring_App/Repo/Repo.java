package com.example.Spring_App.Repo;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Repo {
    List<String> packages;
    List<String> outs;

    public Repo() {
        packages = new ArrayList<>();
        outs = new ArrayList<>();
    }

    public Repo(List<String> packages, List<String> outs) {
        this.packages = packages;
        this.outs = outs;
    }
    public boolean addPkg(String pkg){
        if(!packages.contains(pkg))
            return packages.add(pkg);
        else return false;
    }
    public boolean addOut(String out){
        if(!outs.contains(out))
            return outs.add(out);
        else return false;
    }
    public boolean delPkg(String pkg){
        if(packages.remove(pkg)) return true;
        else return false;
    }
    public boolean delOut(String out){
        if(outs.remove(out)) return true;
        else return false;
    }
    public boolean dellAllPkg(){
        if(!packages.isEmpty())
            return packages.removeAll(packages);
        else return false;
    }
    public boolean dellAllOut(){
        if(!outs.isEmpty())
            return outs.removeAll(outs);
        else return false;
    }

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    public List<String> getOuts() {
        return outs;
    }

    public void setOuts(List<String> outs) {
        this.outs = outs;
    }
}
