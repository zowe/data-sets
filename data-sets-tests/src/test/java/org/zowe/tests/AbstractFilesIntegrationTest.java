/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */
package org.zowe.tests;

import org.zowe.api.common.test.AbstractHttpIntegrationTest;

public abstract class AbstractFilesIntegrationTest extends AbstractHttpIntegrationTest {
    protected static final String HEX_IN_QUOTES_REGEX = "^\"[0-9A-F]+\"$";
}