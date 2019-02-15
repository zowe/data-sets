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
import org.zowe.data.sets.model.AllocationUnitType;
import org.zowe.data.sets.model.DataSetOrganisationType;

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

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface catnm {
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface vols {
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface dev {
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface dsorg {
    }
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface spacu {
    }
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface edate {
    }
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface cdate {
    }
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface lrecl {
    }
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface recfm {
    }
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface sizex {
    }
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface used {
    }

    @dsname
    public String name(JsonObject in) {
        return getStringOrNull(in, "dsname");
    }

    @migr
    public boolean migrated(JsonObject in) {
        return "YES".equals(getStringOrNull(in, "migr"));
    }

    @blksz
    public Integer blocksize(JsonObject in) {
        return getIntegerOrNull(in, "blksz");
    }

    @catnm
    public String catalogName(JsonObject in) {
        return getStringOrNull(in, "catnm");
    }

    @vols
    public String volumeSerial(JsonObject in) {
        return getStringOrNull(in, "vols");
    }

    @dev
    public String deviceType(JsonObject in) {
        return getStringOrNull(in, "dev");
    }

    @dsorg
    public DataSetOrganisationType dataSetOrganization(JsonObject in) {
        DataSetOrganisationType value = null;
        String dsorg = getStringOrNull(in, "dsorg");
        if (dsorg != null) {
            value = DataSetOrganisationType.getByZosmfName(dsorg);
        }
        return value;
    }


    @spacu
    public AllocationUnitType allocationUnit(JsonObject in) {
        AllocationUnitType value = null;
        String spacu = getStringOrNull(in, "spacu");
        if (spacu != null) {
            // SJH : spacu returns a plural string, so strip 's' off the end
            value = AllocationUnitType.valueOf(spacu.substring(0, spacu.length() - 1));
        }
        return value;
    }

    @edate
    public String expirationDate(JsonObject in) {
        return getStringOrNull(in, "edate");
    }

    @cdate
    public String creationDate(JsonObject in) {
        return getStringOrNull(in, "cdate");
    }

    @lrecl
    public Integer recordLength(JsonObject in) {
        return getIntegerOrNull(in, "lrecl");
    }

    @recfm
    public String recordFormat(JsonObject in) {
        return getStringOrNull(in, "recfm");
    }

    @sizex
    public Integer allocatedSize(JsonObject in) {
        return getIntegerOrNull(in, "sizex");
    }

    @used
    public Integer used(JsonObject in) {
        return getIntegerOrNull(in, "used");
    }

    private Integer getIntegerOrNull(JsonObject json, String key) {
        Integer value = null;
        JsonElement jsonElement = json.get(key);
        if (!(jsonElement == null || jsonElement.isJsonNull() || jsonElement.getAsString().equals("?"))) {
            value = jsonElement.getAsInt();
        }
        return value;
    }

    private String getStringOrNull(JsonObject json, String key) {
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