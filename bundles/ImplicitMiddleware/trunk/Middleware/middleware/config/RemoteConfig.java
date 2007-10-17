package middleware.config;

import java.util.Hashtable;

/**
 * Dummy Class
 */
public class RemoteConfig {
   private static RemoteConfig instance = null;
   private        Hashtable transportAttributes; // <String, String[]>
   private        Hashtable transportProtocol;   // <String, String>

   public static RemoteConfig getInstance()
   {
      return instance;
   }

   public String getTransportName(Short hostname) {
      return (String)transportProtocol.get(hostname);
   }

   public String[] getTransportAttributes(Short hostname) {
      return (String[])transportAttributes.get(hostname);
   }
}

