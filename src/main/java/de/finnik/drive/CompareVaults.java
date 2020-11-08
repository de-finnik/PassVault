package de.finnik.drive;

import de.finnik.passvault.passwords.Password;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CompareVaults {
    /**
     * Compares {@link Password} objects of multiple list and if a password has multiple occurrences, the one that has been modified the most recently
     * will be returned among the ones with just one occurrence.
     *
     * @param passwords Lists of {@link Password} objects
     * @return The compared list of {@link Password} objects
     */
    @SafeVarargs
    public static List<Password> compare(List<Password>... passwords) {
        List<Password> allPasswords = new ArrayList<>();
        Arrays.stream(passwords).forEach(allPasswords::addAll);
        List<Password> compared = new ArrayList<>();
        List<String> IDs = allPasswords.stream().map(Password::id).distinct().collect(Collectors.toList());
        for (String id : IDs) {
            allPasswords.stream().filter(password -> password.id().equals(id)).max(Comparator.comparing(Password::lastModified)).ifPresent(compared::add);
        }
        return compared;
    }

    public static String[] changeLog(List<Password> pre, List<Password> post) {
        List<String> changeLog = new ArrayList<>();
        for (Password postPass : post) {
            if (pre.stream().map(Password::id).anyMatch(p -> p.equals(postPass.id()))) {
                // Exists
                if (!pre.contains(postPass)) {
                    // Changed
                    Password prePass = pre.stream().filter(p -> p.id().equals(postPass.id())).findFirst().orElse(null);
                    assert prePass != null;
                    // Deleted
                    if (postPass.isEmpty()) {
                        changeLog.add(Password.log(prePass, "%s -> %s: Deleted password"));
                        continue;
                    }
                    if (!prePass.getPass().equals(postPass.getPass())) {
                        changeLog.add(Password.log(prePass, "%s -> %s: Changed 'password' in password"));
                    }
                    if (!prePass.getSite().equals(postPass.getSite())) {
                        changeLog.add(Password.log(prePass, "%s -> %s: Changed 'site' to '" + postPass.getSite() + "' in password"));
                    }
                    if (!prePass.getUser().equals(postPass.getUser())) {
                        changeLog.add(Password.log(prePass, "%s -> %s: Changed 'user' to '" + postPass.getUser() + "' in password"));
                    }
                    if (!prePass.getOther().equals(postPass.getOther())) {
                        changeLog.add(Password.log(prePass, "%s -> %s: Changed 'other' to '" + postPass.getOther() + "' in password"));
                    }
                }
            } else {
                // Created
                changeLog.add(Password.log(postPass, "%s -> %s: Created password"));
            }
        }
        return changeLog.toArray(new String[0]);
    }
}
