package session;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class SessionManager {

    private static Map<String, String> session = new HashMap<>();
    private static String currentToken;

    public static String createSession(String username) {
        String token = UUID.randomUUID().toString();
        session.put(token, username);
        currentToken = token; // сохраняем текущий активный токен
        return token;
    }

    public static String getUsernameByToken(String token) {
        return session.get(token);
    }

    public static String getLoggedInUserLogin() {
        return session.get(currentToken);
    }

    public static void invalidateSession(String token) {
        session.remove(token);
        if (token.equals(currentToken)) {
            currentToken = null;
        }
    }
    public static void updateSessionLogin(String token, String newLogin) {
        if (session.containsKey(token)) {
            // Обновляем логин в сессии
            session.put(token, newLogin);
        } else {
            System.out.println("Токен сессии не найден");
        }
    }
}
