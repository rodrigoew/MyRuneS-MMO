package com.client;

import javax.swing.JOptionPane;

public final class JVMVersionChecker {
    public static void checkVersion() {
        // version check disabled for local dev
    }

    private static void showError() {
        JOptionPane.showMessageDialog(
            null,
            "FATAL ERROR: Required java version is 13, current java version is "
                + System.getProperty("java.version")
                + ". \nPlease download the JDK specified in the #-downloads section of the discord server."
                + " \n\nIf you followed the instructions above and you are still getting this error,\ntry to right-click"
                + " the jar file > Open With > Zulu Platform x64 Architecture",
            "FATAL ERROR: Wrong java version",
            JOptionPane.ERROR_MESSAGE
        );
    }

    // Can't construct
    private JVMVersionChecker() { }
}
