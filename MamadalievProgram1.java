import java.util.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MamadalievProgram1 {

    private static boolean isLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    // Keeps only the letters of the key, converted to uppercase.
    private static String cleanKey(String key) {
        StringBuilder sb = new StringBuilder();
        for (char c : key.toCharArray()) {
            if (isLetter(c)) {
                sb.append(Character.toUpperCase(c));
            }
        }
        return sb.toString();
    }

    // Gives the new letter the same case as the original letter.
    private static char matchCase(char original, char newUpper) {
        return Character.isUpperCase(original) ? newUpper : Character.toLowerCase(newUpper);
    }

    // ---------- 1. Keyword substitution cipher ----------

    // Builds the 26-letter cipher alphabet: the keyword's letters first , then every unused letter in order.

    private static String encoder(String key) {
        StringBuilder encoded = new StringBuilder();
        boolean[] used = new boolean[26];

        for (char c : key.toCharArray()) {
            if (isLetter(c)) {
                int idx = Character.toUpperCase(c) - 'A';
                if (!used[idx]) {
                    encoded.append((char) ('A' + idx));
                    used[idx] = true;
                }
            }
        }

        for (int i = 0; i < 26; i++) {
            if (!used[i]) {
                encoded.append((char) ('A' + i));
            }
        }
        return encoded.toString();
    }

    public static String keyword(String key, String plaintext) {
        String cipherAlpha = encoder(key);
        StringBuilder out = new StringBuilder();
        for (char ch : plaintext.toCharArray()) {
            if (isLetter(ch)) {
                char sub = cipherAlpha.charAt(Character.toUpperCase(ch) - 'A');
                out.append(matchCase(ch, sub));
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }

    public static String keywordDecrypt(String key, String ciphertext) {
        String cipherAlpha = encoder(key);

        // position of every cipher letter in the cipher alphabet
        Map<Character, Integer> pos = new HashMap<>();
        for (int i = 0; i < cipherAlpha.length(); i++) {
            pos.put(cipherAlpha.charAt(i), i);
        }

        StringBuilder out = new StringBuilder();
        for (char ch : ciphertext.toCharArray()) {
            if (isLetter(ch)) {
                int p = pos.get(Character.toUpperCase(ch));
                out.append(matchCase(ch, (char) ('A' + p)));
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }

    // ---------- 2. Columnar cipher ----------

    /** Column numbers (0-based) in the order they are read: sorted by key letter, ties left to right. */
    private static Integer[] columnOrder(String key) {
        final String k = cleanKey(key);
        Integer[] order = new Integer[k.length()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (a, b) -> {
            int cmp = Character.compare(k.charAt(a), k.charAt(b));
            return cmp != 0 ? cmp : Integer.compare(a, b);
        });
        return order;
    }

    public static String columnar(String key, String plaintext) {
        Integer[] order = columnOrder(key);
        int n = order.length;
        if (n == 0) {
            return plaintext;
        }

        StringBuilder cipher = new StringBuilder();
        for (int c : order) {                                       // go through the columns in key order
            for (int i = c; i < plaintext.length(); i += n) {      // column c is every n-th character, starting at position c
                cipher.append(plaintext.charAt(i));
            }
        }
        return cipher.toString();
    }

    public static String columnarDecrypt(String key, String ciphertext) {
        Integer[] order = columnOrder(key);
        int n = order.length;
        if (n == 0) {
            return ciphertext;
        }

        char[] result = new char[ciphertext.length()];
        int pos = 0;                                          // where we are in the ciphertext
        for (int c : order) {                                // same column order as encryption
            for (int i = c; i < result.length; i += n) {    // put the next ciphertext characters back into column c's positions
                result[i] = ciphertext.charAt(pos);
                pos++;
            }
        }
        return new String(result);
    }

    // ---------- 3. Vigenere cipher ----------

    /** Lines the key up with the text: each letter in the text gets the next key
     * letter (repeating the key); non-letters get a blank and don't use up a key letter. */


    private static String generateKey(String text, String key) {
        String k = cleanKey(key);
        StringBuilder sb = new StringBuilder();
        int j = 0;
        for (char ch : text.toCharArray()) {
            if (isLetter(ch)) {
                sb.append(k.charAt(j % k.length()));
                j++;
            } else {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    public static String vigenere(String key, String plaintext) {
        if (cleanKey(key).isEmpty()) {
            return plaintext;
        }
        String fullKey = generateKey(plaintext, key);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < plaintext.length(); i++) {
            char ch = plaintext.charAt(i);
            if (isLetter(ch)) {
                char base = Character.isUpperCase(ch) ? 'A' : 'a';
                int shift = fullKey.charAt(i) - 'A';
                out.append((char) (base + (ch - base + shift) % 26));
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }

    public static String vigenereDecrypt(String key, String ciphertext) {
        if (cleanKey(key).isEmpty()) {
            return ciphertext;
        }
        String fullKey = generateKey(ciphertext, key);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < ciphertext.length(); i++) {
            char ch = ciphertext.charAt(i);
            if (isLetter(ch)) {
                char base = Character.isUpperCase(ch) ? 'A' : 'a';
                int shift = fullKey.charAt(i) - 'A';
                out.append((char) (base + (ch - base - shift + 26) % 26));
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }


    // Prints the prompt until a valid choice is typed. Returns null if input runs out
    private static String askChoice(Scanner in, String prompt, List<String> valid) {
        while (true) {
            System.out.println(prompt);
            if (!in.hasNextLine()) {
                return null;
            }
            String choice = in.nextLine().trim();
            if (valid.contains(choice)) {
                return choice;
            }
            System.out.println("Invalid choice, please try again.");
        }
    }

    // input checking

    private static void printAllowedCharacters(boolean punctuationToo) {
        System.out.println("Please enter English characters only.");
        System.out.println("Letters: ABCDEFGHIJKLMNOPQRSTUVWXYZ or abcdefghijklmnopqrstuvwxyz");
        System.out.println("Numbers: 0123456789");
        if (punctuationToo) {
            System.out.println("Also allowed: space . ? ! ,");
        }
    }

    private static boolean isEnglishLetterOrDigit(char c) {
        return isLetter(c) || (c >= '0' && c <= '9');
    }

    /* Asks for the keyword until it is valid. Returns null if input runs out. */
    private static String readKey(Scanner in) {
        while (true) {
            System.out.println("Please provide a keyword");
            if (!in.hasNextLine()) {
                return null;
            }
            String key = in.nextLine();


            boolean hasLetter = false;
            boolean allowed = true;

            for (char c : key.toCharArray()) {
                if (isLetter(c)) {
                    hasLetter = true;
                } else if (!isEnglishLetterOrDigit(c) && c != ' ') {
                    allowed = false;
                }
            }


            if (key.trim().isEmpty()) {
                System.out.println("The keyword cannot be empty. Please enter at least one letter.");
            } else if (!allowed) {
                printAllowedCharacters(false);
            } else if (!hasLetter) {
                System.out.println("The keyword must contain at least one letter (numbers alone are not allowed).");
            } else {
                return key;
            }
        }
    }

    /** Asks for the text until it only has allowed characters. Returns null if input runs out. */
    private static String readText(Scanner in, boolean encrypt) {
        while (true) {
            System.out.println("Please provide text to " + (encrypt ? "encipher" : "decipher"));
            if (!in.hasNextLine()) {
                return null;
            }
            String text = in.nextLine();

            boolean allowed = true;
            for (char c : text.toCharArray()) {
                if (!isEnglishLetterOrDigit(c) && " .?!,".indexOf(c) < 0) {
                    allowed = false;
                }
            }

            if (allowed) {
                return text;
            }
            printAllowedCharacters(true);
        }
    }


//-------------------------

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String[] names = {"Keyword", "Columnar", "Vigenere"};

        while (true) {
            String cipher = askChoice(in,
                    "Please select a cipher to apply:\n"
                            + "1. Keyword Cipher\n"
                            + "2. Columnar Cipher\n"
                            + "3. Vigenere Cipher\n"
                            + "0. Quit",
                    Arrays.asList("0", "1", "2", "3"));
            if (cipher == null || cipher.equals("0")) {
                break;
            }

            String mode = askChoice(in, "Please select\n1. Encrypt\n2. Decrypt", Arrays.asList("1", "2"));
            if (mode == null) {
                break;
            }
            boolean encrypt = mode.equals("1");

            String key = readKey(in);
            if (key == null) {
                break;
            }

            String text = readText(in, encrypt);
            if (text == null) {
                break;
            }

            System.out.println("--------------------------------------------");

            System.out.println("Starting " + names[Integer.parseInt(cipher) - 1] + " Cipher"
                    + (encrypt ? "" : " Decryption"));

            String result;
            switch (cipher) {
                case "1":
                    result = encrypt ? keyword(key, text) : keywordDecrypt(key, text);
                    break;
                case "2":
                    result = encrypt ? columnar(key, text) : columnarDecrypt(key, text);
                    break;
                default:
                    result = encrypt ? vigenere(key, text) : vigenereDecrypt(key, text);
                    break;
            }
            System.out.println(text);
            System.out.println(result);
            System.out.println();
        }
        in.close();
    }
}
