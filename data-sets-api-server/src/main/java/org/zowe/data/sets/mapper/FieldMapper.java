/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */

package org.zowe.data.sets.mapper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.mapstruct.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class FieldMapper {
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface dsname {
    }
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface migr {
    }
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface blksz {
    }

    @dsname
    public String name(JsonObject in) {
        return getStringOrNull(in,"dsname");
    }

    @migr
    public boolean migrated(JsonObject in) {
        return "YES".equals(getStringOrNull(in,"migr"));
    }

    @blksz
    public Integer blocksize(JsonObject in) {
        return getIntegerOrNull(in,"blksz");
    }

    private  Integer getIntegerOrNull(JsonObject json, String key) {
        Integer value = null;
        JsonElement jsonElement = json.get(key);
        if (!(jsonElement == null || jsonElement.isJsonNull() || jsonElement.getAsString().equals("?"))) {
            value = jsonElement.getAsInt();
        }
        return value;
    }
    private  String getStringOrNull(JsonObject json, String key) {
        String value = null;
        JsonElement jsonElement = json.get(key);
        if (!(jsonElement == null || jsonElement.isJsonNull() || jsonElement.getAsString().equals("?"))) {
            value = jsonElement.getAsString();
            if (value.equals("?")) {
                value = null;
            }
        }
        return value;
    }

}