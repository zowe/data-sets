package org.zowe.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

@Component
public class GZipServletFilter implements Filter {

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

      if ( acceptsGZipEncoding(httpRequest, httpResponse) ) {
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
        String acceptEncoding = httpRequest.getHeader("Accept-Encoding");
        String accept = httpRequest.getHeader("accept");
        String contentEncoding = httpResponse.getHeader("Content-Encoding");
        
        return acceptEncoding != null && 
               acceptEncoding.indexOf("gzip") != -1 && 
               accept != null && 
               accept.indexOf("application/json") != -1 &&
                       (contentEncoding == null || contentEncoding.indexOf("gzip") != -1);
    }
  }