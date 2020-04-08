package de.finnik.passvault;

import com.google.gson.*;
import de.finnik.gui.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

import static de.finnik.gui.Var.*;

public class PassAPI {

    public PassAPI(String[] args) {
        Method method = null;
        try {
            method = PassAPI.class.getDeclaredMethod(args[0], String[].class);
            if (!method.isAnnotationPresent(Command.class))
                throw new NoSuchMethodException();
            method.invoke(
                    null, (Object) Arrays.copyOfRange(args, 1, args.length));
        } catch (NoSuchMethodException e) {
            printHelp(args[0]);
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.out.printf("Help for command: \n\t%s <input>\n\t\t\t%s\n\t\t%s\n", method.getName(), method.getDeclaredAnnotation(Command.class).description(), String.join("\n\t\t", parameters(args[0])));
        }
    }

    @Command(description = "Add a password to the passwords list")
    private static void add(String[] args) {
        Password password = null;
        if (Arrays.asList(args).contains("-n")) {
            Map<String, String> keyVals = new HashMap<>();
            String[] split;
            for (String arg : args) {
                split = arg.split("=");
                if (split.length == 2) {
                    keyVals.put(split[0], split[1]);
                } else {
                    error(20, String.format("Input parameter %s doesn't contain a key and a value separated by '='", arg));
                }
            }

            password = new Password(keyVals.getOrDefault("pass", ""), keyVals.getOrDefault("site", "")
                    , keyVals.getOrDefault("user", ""), keyVals.getOrDefault("other", ""));
            for (String s : keyVals.keySet().stream()
                    .filter(s -> !Arrays.asList("pass", "site", "user", "other").contains(s))
                    .collect(Collectors.toList())) {
                error(21, String.format("Invalid input parameter: %s", s));
            }
            if (password.emptyParameters() == 4) {
                error(23, "No parameters were given!");
            }
        } else {
            try {
                password = new Gson().fromJson(args[0], Password.class);
            } catch (JsonSyntaxException e) {
                error(24, "Input wasn't adequate to the .json format!");
            }
        }

        assert password != null;

        Password finalPassword = password;
        new PassVault.CheckFrame((pass, passwords) -> {
            passwords.add(finalPassword);
            Password.savePasswords(passwords, PASSWORDS, pass);
            LOG.info(Password.log(finalPassword, "User added password"));
        }).setVisible(true);
    }

    @Command(description = "Search a password matching with the given parameter")
    private static void get(String[] args) {
        if (args[0].length() < 3) {
            error(11, "The input has must be at least three characters long");
        }
        new PassVault.CheckFrame((pass, passwords) -> {
            List<Password> matching = PassUtils.getAllMatchingPasswords(args[0], passwords);
            if (matching.size() == 0) {
                error(10, "No matching passwords were found");
            }
            if (Arrays.asList(args).contains("-n")) {
                for (Password password : matching) {
                    for (String arg : Arrays.copyOfRange(args, Arrays.asList(args).indexOf("-s") + 1, args.length)) {
                        switch (arg) {
                            case "pass":
                                System.out.println(password.getPass());
                                break;
                            case "user":
                                System.out.println(password.getUser());
                                break;
                            case "site":
                                System.out.println(password.getSite());
                                break;
                            case "other":
                                System.out.println(password.getOther());
                                break;
                        }
                    }
                }
            } else {
                GsonBuilder builder = new GsonBuilder();
                if (Arrays.asList(args).contains("-p")) {
                    builder.setPrettyPrinting();
                }
                System.out.println(builder.create().toJson(matching));
            }
            LOG.info("User got information about passwords containing {}!", args[0]);
        }).setVisible(true);
    }

    @Command(description = "Get information about the installed version of PassVault")
    private static void version(String[] args) {
        System.out.printf("%s %s by %s", APP_INFO.getProperty("app.name"), APP_INFO.getProperty("app.version"), APP_INFO.getProperty("app.author"));
        LOG.info("User got information about the installed version of PassVault!");
    }

    private static void error(int code, String message) {
        System.out.printf("!%d %s", code, message);
        System.exit(0);
    }

    private static String[] parameters(String command) {
        List<String> lines = null;
        switch (command) {
            case "add":
                lines = Collections.singletonList("-n\tNo.json format. Set the parameters via: <parameter>=<value>, e.g. ...add -n pass=test123 user=John");
                break;
            case "get":
                lines = Arrays.asList("-p\tEnables pretty printing for the output .json object",
                        "-n\tNo .json format. Append keys to your input, whose values will be printed in the given order,",
                        "\te.g.\tInput:\t...get gmail -n pass user",
                        "\t\t\tOutput:\ttest123",
                        "\t\t\t\t\tJohn");
                break;
        }
        assert lines != null;
        return lines.toArray(new String[0]);
    }

    private void printHelp(String command) {
        System.out.printf("%s is not a valid command! You should probably use one of the following commands:\n\n", command);
        Arrays.stream(PassAPI.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Command.class))
                .forEach(method -> System.out.printf("\t%s \n\t\t\t %s\n", method.getName(), method.getDeclaredAnnotation(Command.class).description()));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private @interface Command {
        String description();
    }
}
