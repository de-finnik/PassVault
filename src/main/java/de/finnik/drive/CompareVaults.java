package de.finnik.drive;

import de.finnik.passvault.*;

import java.util.*;
import java.util.stream.*;

public class CompareVaults {
    @SafeVarargs
    public static List<Password> compare(List<Password>... passwords) {
        List<Password> allPasswords = new ArrayList<>();
        Arrays.stream(passwords).forEach(allPasswords::addAll);
        /*Map<String, List<Password>> passwordsWithIDs = new HashMap<>();
        Arrays.stream(passwords).forEach(list->list.forEach(password -> {
                    if(passwordsWithIDs.containsKey(password.id())) {
                        System.out.println(passwordsWithIDs.get(password.id()));
                        passwordsWithIDs.get(password.id()).add(password);
                    } else {
                        passwordsWithIDs.put(password.id(), Arrays.asList(password));
                    }
                }));
        List<Password> newList = new ArrayList<>();
        for (String id : passwordsWithIDs.keySet()) {
            newList.add(passwordsWithIDs.get(id).stream().max(Comparator.comparing(Password::lastModified)).orElse(null));
        }*/
        List<Password> compared = new ArrayList<>();
        List<String> IDs = allPasswords.stream().map(Password::id).distinct().collect(Collectors.toList());
        for (String id : IDs) {
            allPasswords.stream().filter(password -> password.id().equals(id)).max(Comparator.comparing(Password::lastModified)).ifPresent(compared::add);
        }
        return compared;
    }
}
