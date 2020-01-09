/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2020
 */
package org.zowe.data.sets.model;

import com.google.gson.JsonObject;

public class ZosmfRenameRequest {
    private static final String REQUEST_TYPE = "rename";
    private String dsn;
    private String member;
    
    private ZosmfRenameRequest(String dsn, String member) {
        this.dsn = dsn;
        this.member = member;
    }
    
    public static ZosmfRenameRequest createFromDataSetRenameRequest(String name) {
        
       String dsn;
       String member;
       
       if (hasMember(name)) {
           dsn=parseDsn(name);
           member=parseMember(name);
       } else {
           dsn=name;
           member="";
       }
        
        return new ZosmfRenameRequest(dsn, member);
    }
    
    
    public String getDsn() {
        return dsn;
    }


    public String getMember() {
        return member;
    }


    public static String parseDsn(String name) {
        return name.substring(0,name.indexOf('('));
    }
    
    public static String parseMember(String name) {
        return name.substring(name.indexOf('(')+1,name.indexOf(')'));
    }
    
    public static boolean hasMember(String name) {
        return name.contains("(") && name.contains(")");
    }
    
    public JsonObject buildJson() {
        JsonObject renameJObj = new JsonObject();
        
        JsonObject fromJObj = new JsonObject();
        fromJObj.addProperty("dsn", dsn);
        if(!member.isEmpty()) {
          fromJObj.addProperty("member", member);  
        }
        
        renameJObj.addProperty("request", REQUEST_TYPE);
        renameJObj.add("from-dataset",fromJObj) ;
        
        return renameJObj;
    }
    
    @Override
    public String toString() {
        return buildJson().toString();
    } 
    
}