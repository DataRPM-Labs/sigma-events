/**
 * 
 */
package com.datarpm.sigma.event.core;

/**
 * @author vishal
 *
 */
public interface EventMatchFilter {

  public boolean allow(EventHeader eventHeader);
}
