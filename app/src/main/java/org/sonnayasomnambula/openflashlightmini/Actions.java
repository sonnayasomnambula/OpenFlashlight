package org.sonnayasomnambula.openflashlightmini;

final class Actions {
    public final static String MESSAGE =
            "org.sonnayasomnambula.openflashlightmini.action.message";

    class Message {
        public final static String EXTRA_ID = "id";
        public final static int ID_FAILED_TO_START = 100;
        public final static int ID_STARTED_SUCCESSFULLY = 130;
        public final static int ID_ALREADY_RUNNING = 140;

        public final static String EXTRA_TEXT = "text";
    }

    public final static String STOP =
            "org.sonnayasomnambula.openflashlightmini.action.stop";
}