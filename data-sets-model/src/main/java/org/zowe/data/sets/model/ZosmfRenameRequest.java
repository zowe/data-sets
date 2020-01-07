/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */
package org.zowe.data.sets.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@JsonInclude(Include.NON_NULL)
public class ZosmfRenameRequest {
    private final String request = "rename";
    private String dsn;
    private String member;
   
    public static ZosmfRenameRequest createFromDataSetRenameRequest(String name) {
        
       String dsn;
       String member;
       
       if(hasMember(name)) {
           dsn=parseDsn(name);
           member=parseMember(name);
       } else {
           dsn=name;
           member="";
       }
        
        ZosmfRenameRequestBuilder builder = builder()
                .dsn(dsn)
                .member(member);

        return builder.build();
    }
    
    
    
    public static String parseDsn(String name) {
        return name.substring(0,name.indexOf('('));
    }
    
    public static String parseMember(String name) {
        return name.substring(name.indexOf('(')+1,name.indexOf(')'));
    }
    
    public static Boolean hasMember(String name) {
        return name.contains("(") && name.contains(")");
    }
    
    @Override
    public String toString() {
        if(member.isEmpty()) {
            return "{\"request\":\"rename\", \"from-dataset\":{\"dsn\":\""+dsn+"\"} }";
        }
        return "{\"request\":\"rename\", \"from-dataset\":{\"dsn\":\""+dsn+"\", \"member\":\""+member+"\"} }"; 
    } 
    
}