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

import lombok.AllArgsConstructor;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class GZipServletFilter implements Filter {
    
    private List<String> mimeTypes;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    public void doFilter(ServletRequest request, 
                         ServletResponse response,
                         FilterChain chain) 
    throws IOException, ServletException {

      HttpServletRequest  httpRequest  = (HttpServletRequest)  request;
      HttpServletResponse httpResponse = (HttpServletResponse) response;

      if (acceptsGZipEncoding(httpRequest, httpResponse) ) {
        httpResponse.addHeader("Content-Encoding", "gzip");
        GZipServletResponseWrapper gzipResponse =
          new GZipServletResponseWrapper(httpResponse);
        chain.doFilter(request, gzipResponse);
        gzipResponse.close();
      } else {
        chain.doFilter(request, response);
      }
    }

    private boolean acceptsGZipEncoding(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String reqAcceptEncoding = httpRequest.getHeader("Accept-Encoding");
        String reqAccept = httpRequest.getHeader("Accept");
        List<String> acceptList = Arrays.asList(reqAccept.split("\\s*,\\s*"));
        Set<String> accepted = Stream.concat(acceptList.stream(), mimeTypes.stream())
                .filter(acceptList::contains)
                .filter(mimeTypes::contains)
                .collect(Collectors.toSet());
        
        return (reqAcceptEncoding != null && reqAcceptEncoding.indexOf("gzip") != -1) && accepted.size() > 0;
    }
  }