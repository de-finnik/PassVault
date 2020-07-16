package de.finnik.gui.hints;

import de.finnik.gui.PassVault;
import de.finnik.passvault.utils.Utils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class HintBrowser {
    public static void show(Window owner, String t, String language) throws IOException {
        JDialog frame = new JDialog(owner);
        frame.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        frame.setSize(800, 600);
        JEditorPane editorPane = new JEditorPane();
        editorPane.setPage(PassVault.class.getResource(String.format("/help/%s_%s.html", t, language)));
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
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(editorPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
