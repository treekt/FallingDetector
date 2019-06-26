package pl.treekt.fallingdetector.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DetectorDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "detector.db";
    private static final int DATABASE_VERSION = 1;

    public DetectorDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        final String SQL_CREATE_DETECTOR_TABLE =
                "CREATE TABLE " + DetectorContract.DetectorEntry.TABLE_NAME + " (" +
                        DetectorContract.DetectorEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DetectorContract.DetectorEntry.COLUMN_PHONE_NUMBER + " INT NOT NULL ON CONFLICT REPLACE, " +
                        DetectorContract.DetectorEntry.COLUMN_FIRSTNAME + " TEXT, " +
                        DetectorContract.DetectorEntry.COLUMN_LASTNAME + " TEXT, " +
                        DetectorContract.DetectorEntry.COLUMN_SELECTED + " INTEGER NOT NULL)";

        database.execSQL(SQL_CREATE_DETECTOR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + DetectorContract.DetectorEntry.TABLE_NAME);
        onCreate(database);
    }
}
