package no.shitt.myshit.helper;

public class StringUtil {
    public static String stringWithDefault(String str, String def) {
        return (str == null || str.isEmpty()) ? def : str;
    }

    public static void appendWithLeadingSeparator(StringBuilder sb, String str, String sep, boolean appendBlank) {
        if (str == null || str.isEmpty()) {
            if (appendBlank) {
                sb.append(sep);
            }
        } else {
            sb.append(sep);
            sb.append(str);
        }
    }

    public static boolean equal(String str1, String str2) {
        return (str1 == null ? str2 == null : str1.equals(str2));
    }
}
