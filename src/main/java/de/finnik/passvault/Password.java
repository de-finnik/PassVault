package de.finnik.passvault;

import com.google.gson.*;
import de.finnik.AES.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.stream.*;

import static de.finnik.gui.Var.*;

/**
 * A password object has four parameters:
 * 1. The password
 * 2. The website to which the password belongs
 * 3. The email/username that is used among the password
 * 4. Other information that is useful to know among the password
 */
public class Password {
    private String pass, site, user, other;

    public Password(String pass, String site, String user, String other) {
        this.pass = pass;
        this.site = site;
        this.user = user;
        this.other = other;
    }

    private Password() {
        pass = "";
        site = "";
        user = "";
        other = "";
    }

    /**
     * Encrypts all {@link Password} objects with a given password and saves them in a given file,
     *
     * @param passwords List of {@link Password} objects
     * @param file      The file to save the encrypted passwords to
     * @param pass      The password to encrypt
     */
    public static void savePasswords(List<Password> passwords, File file, String pass) {
        try (AESWriter aesWriter = new AESWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8), new AES(pass))) {
            aesWriter.write(new Gson().toJson(passwords.toArray()));
        } catch (Exception e) {
            LOG.error("Error while saving passwords to {}!", file.getAbsolutePath(), e);
        }
    }

    /**
     * This static method returns all passwords with all of their parameters that are saved to an encrypted file
     *
     * @param file The encrypted file
     * @param pass The password to decrypt
     * @return The List of {@link Password} objects
     */
    public static List<Password> readPasswords(File file, String pass) throws IllegalArgumentException {
        try (AESReader aesReader = new AESReader(new FileReader(file), new AES(pass))) {
            return new ArrayList<>(Arrays.asList(new Gson().fromJson(aesReader.readLine(), Password[].class)));
        } catch (IllegalArgumentException e) {
            // Wrong password
            throw new IllegalArgumentException(e);
        } catch (Exception e) {
            LOG.error("Error while reading passwords from {}!", file.getAbsolutePath(), e);
        }
        return new ArrayList<>();
    }

    /**
     * Creates a string containing all attributes of a {@link Password} objects excluding the password attribute
     *
     * @param password The password to convert to a string
     * @return The converted string
     */
    public static String log(Password password, String message) {
        StringBuilder out = new StringBuilder();
        String separator = ", ";
        if (password.site.length() > 0) {
            out.append("site: ");
            out.append(password.site);
            out.append(separator);
        }
        if (password.user.length() > 0) {
            out.append("user: ");
            out.append(password.user);
            out.append(separator);
        }
        if (password.other.length() > 0) {
            out.append("other information: ");
            out.append(password.other);
            out.append(separator);
        }
        if (out.length() > 0) {
            return String.format("%s: %s!", message, out.substring(0, out.length() - separator.length()));
        } else {
            return message + " with no information!";
        }
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    /**
     * Returns a stream of all parameters
     *
     * @return A stream of all parameters
     */
    public Stream<String> getValues() {
        return Stream.of(pass, site, user, other);
    }

    /**
     * Checks how many parameters of the password equal to ""
     *
     * @return The count of empty parameters
     */
    public int emptyParameters() {
        return (int) Stream.of(pass, site, user, other).filter(s -> s.length() == 0).count();
    }

    /**
     * Checks whether a parameter is equal to ""
     *
     * @param i The index of the parameter (1=pass; 2=site; 3=user; 4=other)
     * @return Parameter equals to ""
     */
    public boolean isEmpty(int i) {
        switch (i) {
            case 0:
                return pass.length() > 0;
            case 1:
                return site.length() > 0;
            case 2:
                return user.length() > 0;
            case 3:
                return other.length() > 0;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == getClass()) {
            Password password = (Password) obj;
            return password.getPass().equals(getPass())
                    && password.getSite().equals(getSite())
                    && password.getUser().equals(getUser())
                    && password.getOther().equals(getOther());
        }
        return false;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pass, site, user, other);
    }

}
