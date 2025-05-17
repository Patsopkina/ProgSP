package session;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class SessionManager {

    private  static  Map<String,String>session = new HashMap<>();
    public static String createSession(String username){
        String token = UUID.randomUUID().toString();
        session.put(token,username);
        return token;
    }
    public static String getUsernameByToken(String token){
        return session.get(token);

    }
public static void invalidateSession(String token){
        session.remove(token);
}
}
