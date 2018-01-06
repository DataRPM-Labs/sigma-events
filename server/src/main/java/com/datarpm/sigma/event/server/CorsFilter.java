/**
 * 
 */
package com.datarpm.sigma.event.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author vishal
 *
 */
public class CorsFilter implements Filter {

  @Override
  public void init(FilterConfig arg0) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
  }

  @Override
  public void destroy() {

  }
}
