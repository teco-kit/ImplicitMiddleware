package middleware.config;

import java.util.Hashtable;

/**
 * Dummy
 */
public class RemoteConfig {
   private static RemoteConfig instance = null;
   private        Hashtable transportAttributes; // <String, String[]>
   private        Hashtable transportProtocol;   // <String, String>

   public static RemoteConfig getInstance()
   {
      return instance;
   }

   public String getTransportName(String hostname) {
      return (String)transportProtocol.get(hostname);
   }

   public String[] getTransportAttributes(String hostname) {
      return (String[])transportAttributes.get(hostname);
   }
}

