package com.app.ahgas_9dd0d56.util;

import android.widget.EditText;
import java.util.regex.Pattern;

public class ValidUtil {
    private static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
            + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
            + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
            + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$");
    private static final Pattern TEXT_ONLY_PATTERN = Pattern.compile("[a-zA-Z ]*");
    private static final Pattern NUMBER_ONLY_PATTERN = Pattern.compile("[0-9+]*");
    private static final Pattern NO_SPECIAL_CHAR_PATTERN = Pattern.compile("[A-Za-z0-9 ]*");

    public static boolean checkTextOnly(String string) {
        return TEXT_ONLY_PATTERN.matcher(string).matches();
    }

    public static boolean checkNumberOnly(String string) {
        return NUMBER_ONLY_PATTERN.matcher(string).matches();
    }

    public static boolean checkNoSpecialCharacter(String string) {
        return NO_SPECIAL_CHAR_PATTERN.matcher(string).matches();
    }

    public static boolean checkEmail(String string) {
        return EMAIL_ADDRESS_PATTERN.matcher(string).matches();
    }

    public static boolean passMatch(EditText pass1, EditText pass2){
        return Utils.txt(pass1).equals(Utils.txt(pass2));
    }

}
