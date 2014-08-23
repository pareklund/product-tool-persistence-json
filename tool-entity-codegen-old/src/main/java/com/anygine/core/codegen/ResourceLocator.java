package com.anygine.core.codegen;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores references to objects. 
 * <p>
 * Functions as an extremely simple ServiceLocator in that it stores references
 * to implementation classes that are not directly accessible to other modules.
 * Through the use of a singleton instance of this class, clean module 
 * dependencies can be maintained.
 * <p>
 * TODO: Investigate if this mechanism can be replaced by a declaratively
 *       configurable dependency injection framework.
 *       
 * @author Pär Eklund
 *
 */
public class ResourceLocator {

  private static Map<String, Object> resources = new HashMap<String, Object>();

  /**
   * Sets a resource with the given resourceUrl.
   * <p>
   * In the checkpointing framework, the qualified interface class name is used
   * as resource url.
   * 
   * @param resourceUrl          the identifying resource url
   * @param resource             the resource to set
   */
  public static void setResource(String resourceUrl,
      Object resource) {
    resources.put(resourceUrl, resource);
  }

  /**
   * Gets a resource with the given resourceUrl.
   * <p>
   * @param resourceUrl          the identifiying resource url
   * @return                     the resource to get
   */
  public static Object getResource(String resourceUrl) {
    return resources.get(resourceUrl);
  }

  static {
    // Initialize using test resources that return vanilla classes
    // TODO
    //    	TestCheckpointingHandler.initialize();
  }
}
