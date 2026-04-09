package view;

import javafx.stage.Stage;

/**
 * VIEW LAYER — Abstract base for all screen views.
 *
 * ABSTRACTION  : abstract class — cannot be instantiated directly.
 * INHERITANCE  : all View classes extend this to get shared stage methods.
 * POLYMORPHISM : getTitle() is abstract — each child returns a different title string.
 */
public abstract class BaseView {

    protected Stage stage; // inherited by all child views

    // ABSTRACTION — every child MUST override this
    // POLYMORPHISM — each child returns a different window title
    public abstract String getTitle();

    // ── Methods inherited FREE by all child views ─────────────
    public void show()  { stage.setTitle(getTitle()); stage.show(); }
    public void hide()  { stage.hide(); }
    public void close() { stage.close(); }

    public Stage getStage() { return stage; }

    /** Hook the window's X button to a custom Runnable (e.g. logout confirm). */
    public void setOnCloseRequest(Runnable action) {
        stage.setOnCloseRequest(e -> {
            e.consume();
            if (action != null) action.run();
        });
    }
}
