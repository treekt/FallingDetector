package pl.treekt.fallingdetector.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class DetectorContract {

    public static final String CONTENT_AUTHORITY = "pl.treekt.fallingdetector";
    public static final String PATH_DETECTOR = "detector";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class DetectorEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_DETECTOR)
                .build();

        public static final String TABLE_NAME = "detector";
        public static final String COLUMN_PHONE_NUMBER = "number";
        public static final String COLUMN_FIRSTNAME = "name";
        public static final String COLUMN_LASTNAME = "surname";
        public static final String COLUMN_SELECTED = "selected";
    }
}
