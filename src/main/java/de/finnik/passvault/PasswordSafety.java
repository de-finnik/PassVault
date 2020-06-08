package de.finnik.passvault;

import java.util.Arrays;

public class PasswordSafety {
    public static long crackDuration(String password) {
        int space_depth = Arrays.stream(PasswordGenerator.PassChars.values()).filter(p -> Arrays.stream(p.get().split("")).anyMatch(password::contains)).mapToInt(p -> p.get().length()).sum();
        long possible_password_amount = (long) Math.pow(space_depth, password.length());
        return (long) (possible_password_amount / 17042.497);
    }
}
