package gov.ismonnet.cardhelp.activity;

public interface ActivityLifeCycle {

    default void onCreate() {
    }

    default void onStart() {
    }

    default void onResume() {
    }

    default void onPause() {
    }

    default void onStop() {
    }

    default void onRestart() {
    }

    default void onDestroy() {
    }
}
