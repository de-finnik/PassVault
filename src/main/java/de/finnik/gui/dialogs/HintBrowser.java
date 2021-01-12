package de.finnik.gui.dialogs;

import de.finnik.gui.PassVault;
import de.finnik.passvault.utils.Utils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class HintBrowser {
    /**
     * Opens a html file inside an {@link JEditorPane}
     * <p>
     * The html file will be picked from the directory: /help/${name}_${language}.html
     *
     * @param owner    The window that will own the dialog
     * @param name     The name of the file to be opened
     * @param language The language that will be displayed inside the html file
     * @throws IOException Html file couldn't be found
     */
    public static void show(Window owner, String name, String language) throws IOException {
        JDialog frame = new JDialog(owner);
        frame.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        frame.setSize(800, 600);

        JEditorPane editorPane = new JEditorPane();
        editorPane.setPage(PassVault.class.getResource(String.format("/help/%s_%s.html", name, language)));
        editorPane.setEditable(false);
        editorPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Utils.Browser.browse(e.getURL().toString());
                } catch (URISyntaxException | IOException uriSyntaxException) {
                    uriSyntaxException.printStackTrace();
                }
            }
        });
        editorPane.setBorder(BorderFactory.createEmptyBorder());

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
