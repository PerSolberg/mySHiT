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

    public static void appendWithTrailingSeparator(StringBuilder sb, String str, String sep, boolean appendBlank) {
        if (str == null || str.isEmpty()) {
            if (appendBlank) {
                sb.append(sep);
            }
        } else {
            sb.append(str);
            sb.append(sep);
        }
    }
}
