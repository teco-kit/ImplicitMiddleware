package middleware.config;

import java.util.Hashtable;


/**
 * Dummy Class
 */
public class ProtocolsConfig {
   private static ProtocolsConfig instance = null;

   public static ProtocolsConfig getInstance()
   {
      return instance;
   }

   private ProtocolsConfig()
   {
   }

   public Short getHostname() {
      return null;
   }

   public Hashtable getProtocols() {
      return null;
   }

}

