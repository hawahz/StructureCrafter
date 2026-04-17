package io.github.hawah.structure_crafter.util;

public class StarPattern {
    public static String wildcardToRegex(String pattern) {
        StringBuilder sb = new StringBuilder();
        sb.append("^");

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);

            switch (c) {
                case '*':
                    sb.append(".*");
                    break;

                // 转义正则特殊字符
                case '.', '\\', '+', '?', '^', '$', '(', ')', '[', ']', '{', '}', '|':
                    sb.append("\\").append(c);
                    break;

                default:
                    sb.append(c);
            }
        }

        sb.append("$");
        return sb.toString();
    }

    public static boolean matchWildcard(String pattern, String input) {
        String regex = wildcardToRegex(pattern);
        return input.matches(regex);
    }
}
