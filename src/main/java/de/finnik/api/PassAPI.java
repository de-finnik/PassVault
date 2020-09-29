package de.finnik.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import de.finnik.gui.PassVault;
import de.finnik.passvault.passwords.Password;
import de.finnik.passvault.utils.PassUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.finnik.gui.Var.*;

/**
 * Allows you to kind of manage your passwords out of the command line.
 * If you append arguments to the execution this constructor will be fired.
 *
 * @see PassAPI#PassAPI(String[])
 */
public class PassAPI {

    /**
     * Takes the arguments that are given to the main application
     *
     * @param args The arguments
     * @see PassVault#main(String[])
     */
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

    /**
     * Allows you to add a password to the passwords list just from arguments.
     * Expects a json object as input with keys pass, site, user, other and matching values.
     *
     * @param args The input json object
     */
    @Command(description = "Add a password to the passwords list")
    private static void add(String[] args) {
        Password password = null;

        try {
            password = new Gson().fromJson(args[0], Password.class);
        } catch (JsonSyntaxException e) {
            error(20, "Input wasn't adequate to the .json format!");
        }

        assert password != null;
        if (password.isEmpty()) {
            error(21, "Input password has no valid parameters!");
        }

        Password finalPassword = password;

        PassVault.CheckFrame checkFrame = new PassVault.CheckFrame((pass, passwords) -> {
            passwords.add(finalPassword);
            Password.savePasswords(passwords, PASSWORDS, pass);
            LOG.info(Password.log(finalPassword, "User added password"));
        });
        DIALOG.OWNER = checkFrame;
        checkFrame.setVisible(true);
    }

    /**
     * Allows you to get all passwords matching to a keyword.
     * Expects a keyword as input and optionally -p as argument to enable pretty printing for the output json objects.
     *
     * @param args The key word and if necessary -p option
     */
    @Command(description = "Search a password matching with a given keyword")
    private static void get(String[] args) {
        if (args[0].length() < 3) {
            error(11, "The input must be at least three characters long");
        }
        PassVault.CheckFrame checkFrame = new PassVault.CheckFrame((pass, passwords) -> {
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
        }, String.format(LANG.getString("api.get.warning"), args[0]));
        DIALOG.OWNER = checkFrame;
        checkFrame.setVisible(true);
    }

    /**
     * Allows you to get information about which version of PassVault is installed.
     * Expects no arguments
     *
     * @param args Empty
     */
    @Command(description = "Get information about the installed version of PassVault")
    private static void version(String[] args) {
        System.out.printf("%s %s by %s\n", APP_INFO.getProperty("app.name"), APP_INFO.getProperty("app.version"), APP_INFO.getProperty("app.author"));
        LOG.info("User got information about the installed version of PassVault!");
    }

    /**
     * Will warn the user that the operation wasn't successful and stop the program via {@link System#exit(int)}
     *
     * @param code    The error code
     * @param message The error message to display
     */
    private static void error(int code, String message) {
        System.out.printf("!%d %s\n", code, message);
        System.exit(1);
    }

    /**
     * Prints help for a given command
     *
     * @param command The command to get more information from
     */
    private void printHelp(String command) {
        InputStream inputStream = PassVault.class.getResourceAsStream("/help/api_help/" + command + ".help");
        if (inputStream == null)
            return;
        Arrays.stream(PassAPI.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Command.class) && method.getName().equals(command))
                .map(method -> method.getDeclaredAnnotation(Command.class).description())
                .forEach(description -> System.out.printf(new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining(System.lineSeparator())) + "\n", description));
    }

    /**
     * Prints all valid commands and what they can be used for.
     * Is executed when the user enters an invalid command.
     *
     * @param command The invalid command entered by the user
     */
    private void printCommands(String command) {
        System.out.printf("%s is not a valid command! You should probably use one of the following commands:\n\n", command);
        Arrays.stream(PassAPI.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Command.class))
                .forEach(method -> System.out.printf("\t%s\n\t\t\t%s\n", method.getName(), method.getDeclaredAnnotation(Command.class).description()));

        System.out.println("\t<command> help\n\t\t\tGet help for the command <command>\n");
    }

    /**
     * Marks all valid command functions and informs the user about their functionality
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private @interface Command {
        String description();
    }
}
