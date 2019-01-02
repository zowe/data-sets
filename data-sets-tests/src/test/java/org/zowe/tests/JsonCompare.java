/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2016, 2018
 */
package org.zowe.tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO LATER - delete this mess!

public class JsonCompare {

    public static final boolean STRICT_ORDER = true;
    public static final boolean LAX_ORDER = false;

    public static final boolean SUBSET_EQUAL = true;
    public static final boolean STRICT_EQUAL = false;

    public static String compare(JsonElement expectedResult, JsonElement actualResult,
            Map<String, String> substitutionVars, boolean treatExpectedValueAsRegex,
            boolean useStrictArrayOrderCompare) {
        String result = null;
        result = compareJsonArtifacts(expectedResult, actualResult);
        if (null != result)
            return result;
        if (expectedResult.isJsonArray())
            result = compareJsonArrays(expectedResult.getAsJsonArray(), actualResult.getAsJsonArray(), substitutionVars,
                    treatExpectedValueAsRegex, useStrictArrayOrderCompare);
        else
            result = compareJsonObjects(expectedResult.getAsJsonObject(), actualResult.getAsJsonObject(),
                    substitutionVars, treatExpectedValueAsRegex, useStrictArrayOrderCompare);

        return result;
    }

    private static String compareValues(String baseLocation, Object expectedResultValue, Object actualResultValue,
            Map<String, String> substitutionVars, boolean treatExpectedValueAsRegex, boolean allowUnorderedArrays) {
        if (expectedResultValue instanceof JsonObject) {
            if (!(actualResultValue instanceof JsonObject)) {
                return "JSON types don't match: " + baseLocation + ". Value: \"" + expectedResultValue
                        + "\". Expected Result object is JSONObject but Actual Result is \""
                        + actualResultValue.getClass().getSimpleName() + "\"";
            } else {
                return compareJsonObjects((JsonObject) expectedResultValue, (JsonObject) actualResultValue,
                        substitutionVars, treatExpectedValueAsRegex, allowUnorderedArrays);
            }
        } else if (expectedResultValue instanceof JsonArray) {
            if (!(actualResultValue instanceof JsonArray)) {
                return "JSON types don't match: " + baseLocation + ". Value: \"" + expectedResultValue
                        + "\". Expected Result object is JSONArray but Actual Result is \""
                        + actualResultValue.getClass().getSimpleName() + "\"";
            } else {
                return compareJsonArrays((JsonArray) expectedResultValue, (JsonArray) actualResultValue,
                        substitutionVars, treatExpectedValueAsRegex, allowUnorderedArrays);
            }
        } else if (expectedResultValue instanceof Object) {
            if (!(actualResultValue instanceof Object)) {
                return "JSON types don't match: " + baseLocation + ". Value: \"" + expectedResultValue
                        + "\". Expected Result object is Object but Actual Result is \""
                        + actualResultValue.getClass().getSimpleName() + "\"";

            } else if (treatExpectedValueAsRegex && expectedResultValue instanceof String
                    && actualResultValue instanceof String) {
                if (expectedResultValue.equals(actualResultValue)) {
                    return null;
                }
                // treat the expectedResultValue as a regex and see if the actual result matches
                // or not
                Pattern pattern = Pattern.compile((String) expectedResultValue, Pattern.MULTILINE | Pattern.DOTALL);
                Matcher matcher = pattern.matcher((String) actualResultValue);

                return matcher.matches() ? null
                        : "JSON objects not equal: " + baseLocation + ". Expected Result value is: \""
                                + expectedResultValue + "\" but Actual Result is: \"" + actualResultValue + "\"";

            } else {
                return (expectedResultValue).equals(actualResultValue) ? null
                        : "JSON objects not equal: " + baseLocation + ". Expected Result value is: \""
                                + expectedResultValue + "\" but Actual Result is: \"" + actualResultValue + "\"";
            }
        } else if (expectedResultValue == null) {
            return actualResultValue == null ? null
                    : "JSON objects not equal: " + baseLocation
                            + ". Expected Result value is null but Actual Result is: \"" + actualResultValue + "\"";
        } else {
            return "Expected Result file JSONArtifact contains invalid object for key: " + baseLocation
                    + ". \nObject class is: " + expectedResultValue.getClass().getSimpleName() + "\"";
        }
    }

    public static String compareJsonArrays(JsonArray expectedResult, JsonArray actualResult,
            Map<String, String> substitutionVars, boolean treatExpectedValueAsRegex, boolean allowUnorderedArrays) {
        int i = 0;

        // if JSON arrays are allowed to be unordered, just sort them and let the
        // regular iterator compare handle it
        if (allowUnorderedArrays) {
            Set<JsonElement> expectedSet = setOfElements(expectedResult);
            Set<JsonElement> actualSet = setOfElements(actualResult);
            if (!expectedSet.equals(actualSet)) {
                return "Doesn't match";
            }
            return null;
        }

        for (Object expectedResultValue : expectedResult) {
            if (actualResult.size() != expectedResult.size() || i >= actualResult.size()) {
                return "JSON Arrays are unequal size.  \nExpected Result array: " + expectedResult
                        + "\n  Actual Result array: " + actualResult;
            }
            Object actualResultValue = actualResult.get(i);
            String result = null;
            result = compareValues(" JSONArray index=\"" + String.valueOf(i) + "\"", expectedResultValue,
                    actualResultValue, substitutionVars, treatExpectedValueAsRegex, allowUnorderedArrays);
            if (result != null)
                return result;
            i++;
        }
        return null;
    }

    static Set<JsonElement> setOfElements(JsonArray arr) {
        Set<JsonElement> set = new HashSet<JsonElement>();
        for (JsonElement j : arr) {
            set.add(j);
        }
        return set;
    }

    @SuppressWarnings("unchecked")
    public static String compareJsonObjects(JsonObject expectedResult, JsonObject actualResult,
            Map<String, String> substitutionVars, boolean treatExpectedValueAsRegex, boolean allowUnorderedArrays) {
        int bSize = expectedResult.size();
        int tSize = actualResult.size();
        boolean subset = false;
        if (!subset) {
            if (bSize != tSize) {
                String msg = "JSON objects are not the same size. Expected Result File is \"" + bSize
                        + "\" but Actual Result is \"" + tSize + "\"";
                return msg;
            }
        }

        for (Map.Entry<String, JsonElement> j : expectedResult.entrySet()) {
            String expectedResultKey = j.getKey();
            Object expectedResultValue = j.getValue();

            if (!actualResult.has(expectedResultKey)) {
                return "JSON objects not equal. Expected Result File contains key/value: \"" + expectedResultKey
                        + "\" : \"" + expectedResultValue + "\"." + " This key is missing from the Actual Result";
            }

            // Replace test-specific substitution variables in the expected result file with
            // supplied values
            if (substitutionVars != null && expectedResultValue instanceof String) {
                for (Map.Entry<String, String> entry : substitutionVars.entrySet()) {
                    expectedResultValue = String.valueOf(expectedResultValue)
                            .replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue());
                }
            }

            Object actualResultValue = actualResult.get(expectedResultKey);
            String result = compareValues(" JSONObject key= \"" + expectedResultKey + "\"", expectedResultValue,
                    actualResultValue, substitutionVars, treatExpectedValueAsRegex, allowUnorderedArrays);
            return result;
        }
        return null;
    }

    public static String compareJsonArtifacts(JsonElement expectedResult, JsonElement actualResult) {
        if (expectedResult.isJsonArray() && actualResult.isJsonArray())
            return null;
        else if (expectedResult.isJsonObject() && actualResult.isJsonObject())
            return null;
        else
            return "JSONArtifacts are not of the same type.  " + "Expected Result is "
                    + (expectedResult == null ? "null" : expectedResult.getClass().getSimpleName())
                    + ". Actual Result is " + (actualResult == null ? "null" : actualResult.getClass().getSimpleName());
    }
}
