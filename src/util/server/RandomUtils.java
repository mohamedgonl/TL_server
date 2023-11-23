
package util.server;

public class RandomUtils {

    public static int hashCode(String str) {
        int hash = 0;
        if (str.isEmpty()) return hash;

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            hash = (hash << 5) - hash + ch;
        }
        return hash;
    }
     public static double generateRandomBySeed(int min, int max, String seed, boolean isInteger) {
        double randomNumber = Math.abs(Math.sin(hashCode(seed)));
        double scaledRandom = min + randomNumber * (max - min);
        double rd= isInteger ? Math.round(scaledRandom) : scaledRandom;
        return rd;
    }


}
