package it.ttf.invernizzi.salting.authentication;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    Hash passwords for storage, and test passwords against password tokens.
    Instances of this class can be used concurrently by multiple threads.
*/

public final class PasswordAuthentication {
    // unique token identifier for this class
    public static final String PREFIX_ID = "$0001$";

    public static final int DEFAULT_COST = 16;

    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";

    private static final int SIZE = 128;
    private static final int SALT_SIZE = SIZE/8;

    // used to retrieve info from the stored tokens through regexps
    private static final Pattern layout = Pattern.compile("\\$0001\\$(\\d\\d?)\\$(.{43})");

    private final SecureRandom random;

    private final int cost;

    public PasswordAuthentication()
    {
        this(DEFAULT_COST);
    }

   // Create a password manager with a specified cost
   // cost represents the exponential computational cost of hashing a password, 0 to 30
    public PasswordAuthentication(int cost)
    {
        iterations(cost); // Validate cost
        this.cost = cost;
        this.random = new SecureRandom();
    }

    // cost validation
    private static int iterations(int cost)
    {
        if ((cost < 0) || (cost > 30))
            throw new IllegalArgumentException("cost: " + cost);
        return 1 << cost;
    }

    /*
        Hash a password for storage.
        return a secure authentication token to be stored for later authentication
    */
    public String hash(char[] password)
    {
        byte[] salt = new byte[SALT_SIZE];
        random.nextBytes(salt);
        byte[] dk = pbkdf2(password, salt, 1 << cost);
        byte[] hash = new byte[salt.length + dk.length];
        System.arraycopy(salt, 0, hash, 0, salt.length);
        System.arraycopy(dk, 0, hash, salt.length, dk.length);
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
        return PREFIX_ID + cost + '$' + enc.encodeToString(hash);
    }

    /*
        Authenticate with a password and a stored password token.
        return true if the password and token match
     */
    public boolean authenticate(char[] password, String token)
    {
        Matcher m = layout.matcher(token);
        if (!m.matches())
            throw new IllegalArgumentException("Invalid token format");
        int iterations = iterations(Integer.parseInt(m.group(1)));
        byte[] hash = Base64.getUrlDecoder().decode(m.group(2));
        byte[] salt = Arrays.copyOfRange(hash, 0, SALT_SIZE);
        byte[] check = pbkdf2(password, salt, iterations);
        int zero = 0;
        for (int idx = 0; idx < check.length; ++idx)
            zero |= hash[salt.length + idx] ^ check[idx];
        return zero == 0;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations)
    {
        KeySpec spec = new PBEKeySpec(password, salt, iterations, SIZE);
        try {
            SecretKeyFactory f = SecretKeyFactory.getInstance(ALGORITHM);
            return f.generateSecret(spec).getEncoded();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Missing algorithm: " + ALGORITHM, ex);
        }
        catch (InvalidKeySpecException ex) {
            throw new IllegalStateException("Invalid SecretKeyFactory", ex);
        }
    }

    public static boolean validatePassword(String password) {
    /*  Matches invalid passwords:
        Anything with less than eight characters
        OR anything with no numbers
        OR anything with no uppercase
        OR or anything with no lowercase
        OR anything with no special characters
    */
       Pattern invalidPswdPattern = Pattern.compile("^(.{0,7}|[^0-9]*|[^A-Z]*|[^a-z]*|[a-zA-Z0-9]*)$");
       Matcher matcher = invalidPswdPattern.matcher(password);

       return !matcher.matches();
    }
}