package io.github.hawah.structure_crafter.client.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SearchHelper {
    public static List<String> search(String query, List<String> data) {
        String q = normalize(query);

        List<String> candidates = new ArrayList<>();

        // 阶段1：过滤
        for (String s : data) {
            String t = normalize(s);

            if (t.contains(q) || fuzzyMatch(q, t)) {
                candidates.add(s); // 注意：返回原字符串
            }
        }

        // 阶段2：排序
        candidates.sort(Comparator.comparingDouble(s -> {
            String t = normalize(s);
            return score(q, t);
        }));

        return candidates;
    }

    public static String normalize(String s) {
        return s.toLowerCase(Locale.ROOT)
                .replace("_", "")
                .replace(" ", "");
    }

    public static float score(String q, String t) {
        int lev = levenshtein(q, t);

        float prefix = t.startsWith(q) ? 1.0f : 0.0f;
        float subseq = fuzzyMatch(q, t) ? 1.0f : 0.0f;

        float lenPenalty = Math.abs(t.length() - q.length()) * 0.1f;

        return lev - prefix * 2 - subseq * 1 + lenPenalty;
    }

    public static int levenshtein(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[n][m];
    }

    public static boolean fuzzyMatch(String pattern, String text) {
        int i = 0;
        for (char c : text.toCharArray()) {
            if (i < pattern.length() && c == pattern.charAt(i)) i++;
        }
        return i == pattern.length();
    }
}
