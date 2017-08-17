package org.sonnayasomnambula.openflashlight;

final class Actions {
    public final static String MESSAGE =
            "org.sonnayasomnambula.openflashlight.action.message";

    class Message {
        public final static String EXTRA_ID = "id";
        public final static int ID_FAILED_TO_START = 100;
        public final static int ID_STARTED_SUCCESSFULLY = 130;
        public final static int ID_ABOUT_TO_STOP = 150;

        public final static String EXTRA_TEXT = "text";
    }

    public final static String STOP =
            "org.sonnayasomnambula.openflashlight.action.stop";
}