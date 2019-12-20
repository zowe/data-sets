/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */

package org.zowe.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

class GZipServletOutputStream extends ServletOutputStream {
    private GZIPOutputStream    gzipOutputStream = null;

    public GZipServletOutputStream(OutputStream output)
          throws IOException {
      super();
      this.gzipOutputStream = new GZIPOutputStream(output);
    }

    @Override
    public void close() throws IOException {
      this.gzipOutputStream.close();
    }

    @Override
    public void flush() throws IOException {
      this.gzipOutputStream.flush();
    }

    @Override
    public void write(byte b[]) throws IOException {
      this.gzipOutputStream.write(b);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
      this.gzipOutputStream.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
       this.gzipOutputStream.write(b);
    }

    @Override
    public boolean isReady() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setWriteListener(WriteListener listener) {
        // TODO Auto-generated method stub
        
    }
  }