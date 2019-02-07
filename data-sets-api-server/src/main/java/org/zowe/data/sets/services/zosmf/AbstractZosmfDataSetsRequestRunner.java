/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.data.sets.services.zosmf;

import lombok.extern.slf4j.Slf4j;

import org.zowe.api.common.zosmf.services.AbstractZosmfRequestRunner;

@Slf4j
public abstract class AbstractZosmfDataSetsRequestRunner<T> extends AbstractZosmfRequestRunner<T> {

    static final String AUTHORIZATION_FAILURE = "ISRZ002 Authorization failed";
    static final String DATA_SET_NOT_FOUND = "ISRZ002 Data set not cataloged";

}
