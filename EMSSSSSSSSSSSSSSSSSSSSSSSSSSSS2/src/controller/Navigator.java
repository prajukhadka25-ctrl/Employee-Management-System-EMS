package controller;

import javafx.stage.Stage;

/**
 * CONTROLLER LAYER — Static navigation helper.
 * Keeps track of the previous stage so Back navigation works.
 */
public class Navigator {

    private static Stage previousStage = null;

    /**
     * Navigate forward: hide current stage, show next stage.
     * Saves current so Back can return to it.
     */
    public static void navigateTo(Stage from, Stage to) {
        previousStage = from;
        if (from != null) from.hide();
        to.show();
    }

    /**
     * Go back: hide current, re-show previous.
     */
    public static void goBack(Stage current) {
        if (current != null) current.hide();
        if (previousStage != null) {
            previousStage.show();
            previousStage = null;
        }
    }

    /**
     * Replace: close current permanently, show brand-new stage (e.g. logout → login).
     * Clears the back history.
     */
    public static void replace(Stage closing, Stage newStage) {
        previousStage = null;
        if (closing != null) closing.close();
        newStage.show();
    }

    public static boolean canGoBack() { return previousStage != null; }
}
