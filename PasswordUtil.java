import java.util.HashMap;
import java.util.Map;

public class PasswordUtil {
    // 許可されたユーザーIDとパスワードの組み合わせ
    private static final Map<String, String> VALID_USERS = new HashMap<>();

    static {
        // ご要望いただいた4つのアカウントを登録
        VALID_USERS.put("0001", "pass1");
        VALID_USERS.put("0002", "pass2");
        VALID_USERS.put("0003", "pass3");
        VALID_USERS.put("0004", "pass4");
    }

    // MainAppから呼ばれるログイン判定メソッド
    public static boolean authenticate(String username, String password) {
        // ユーザーが存在し、パスワードが一致するかチェック
        return VALID_USERS.containsKey(username) && VALID_USERS.get(username).equals(password);
    }
}