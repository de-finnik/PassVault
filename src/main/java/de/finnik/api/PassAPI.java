package de.finnik.api;

import com.google.gson.*;
import de.finnik.gui.*;
import de.finnik.passvault.*;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

import static de.finnik.gui.Var.*;

public class PassAPI {

    public PassAPI(String[] args) {
        try {
            Method method = PassAPI.class.getDeclaredMethod(args[0], String[].class);
            if (!method.isAnnotationPresent(Command.class))
                throw new NoSuchMethodException();
            if (Arrays.asList(args).contains("help"))
                throw new IllegalAccessException();
            method.invoke(
                    null, (Object) Arrays.copyOfRange(args, 1, args.length));
        } catch (NoSuchMethodException e) {
            printCommands(args[0]);
        } catch (IllegalAccessException | InvocationTargetException e) {
            printHelp(args[0]);
        }
    }

    @Command(description = "Add a password to the passwords list")
    private static void add(String[] args) {
        Password password = null;

        try {
            password = new Gson().fromJson(args[0], Password.class);
        } catch (JsonSyntaxException e) {
            error(20, "Input wasn't adequate to the .json format!");
        }

        assert password != null;
        if (password.emptyParameters() == 4) {
            error(21, "Input password has no valid parameters!");
        }

        Password finalPassword = password;

        new PassVault.CheckFrame((pass, passwords) -> {
            passwords.add(finalPassword);
            Password.savePasswords(passwords, PASSWORDS, pass);
            LOG.info(Password.log(finalPassword, "User added password"));
        }).setVisible(true);
    }

    @Command(description = "Search a password matching with a given keyword")
    private static void get(String[] args) {
        if (args[0].length() < 3) {
            error(11, "The input must be at least three characters long");
        }
        new PassVault.CheckFrame((pass, passwords) -> {
            List<Password> matching = PassUtils.getAllMatchingPasswords(args[0], passwords);
            if (matching.size() == 0) {
                error(10, "No matching passwords were found");
            }

            GsonBuilder builder = new GsonBuilder();
            if (Arrays.asList(args).contains("-p")) {
                builder.setPrettyPrinting();
            }

            System.out.println(builder.create().toJson(matching));
            LOG.info("User got information about passwords containing {}!", args[0]);
        }, String.format(LANG.getProperty("api.get.warning"), args[0])).setVisible(true);
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

    private void printHelp(String command) {
        InputStream inputStream = PassVault.class.getResourceAsStream("/api_help/" + command + ".help");
        if (inputStream == null)
            return;
        Arrays.stream(PassAPI.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Command.class) && method.getName().equals(command))
                .map(method -> method.getDeclaredAnnotation(Command.class).description())
                .forEach(description -> System.out.printf(new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining(System.lineSeparator())), description));
    }

    private void printCommands(String command) {
        System.out.printf("%s is not a valid command! You should probably use one of the following commands:\n\n", command);
        Arrays.stream(PassAPI.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Command.class))
                .forEach(method -> System.out.printf("\t%s\n\t\t\t%s\n", method.getName(), method.getDeclaredAnnotation(Command.class).description()));

        System.out.println("\t<command> help\n\t\t\tGet help for the command <command>\n");
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private @interface Command {
        String description();
    }
}
