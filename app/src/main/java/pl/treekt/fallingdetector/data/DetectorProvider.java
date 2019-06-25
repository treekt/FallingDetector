package pl.treekt.fallingdetector.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Objects;

public class DetectorProvider extends ContentProvider {

    private static final int CODE_CONTACT_LIST = 100;
    private static final int CODE_SINGLE_CONTACT = 101;
    private static final UriMatcher sUriMatcher = builUriMatcher();
    private DetectorDbHelper mDbHelper;

    private static UriMatcher builUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = DetectorContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, DetectorContract.PATH_DETECTOR, CODE_CONTACT_LIST);
        uriMatcher.addURI(authority, DetectorContract.PATH_DETECTOR + "/#", CODE_SINGLE_CONTACT);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DetectorDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable
            String[] selectionArgs, @Nullable String sortOrder) {

        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case CODE_CONTACT_LIST:
                cursor = mDbHelper.getReadableDatabase().query(
                        DetectorContract.DetectorEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri ");
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri retUri;

        switch (sUriMatcher.match(uri)) {
            case CODE_CONTACT_LIST:
                long insertedId = mDbHelper.getWritableDatabase().insert(
                        DetectorContract.DetectorEntry.TABLE_NAME,
                        null,
                        values);

                if (insertedId > 0) {
                    retUri = ContentUris.withAppendedId(DetectorContract.DetectorEntry.CONTENT_URI, insertedId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknow uri: " + uri);
        }

        Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);

        return retUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int deletedContact;

        final SQLiteDatabase database = mDbHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case CODE_SINGLE_CONTACT:
                String id = uri.getLastPathSegment();
                String tableName = DetectorContract.DetectorEntry.TABLE_NAME;

                deletedContact = database.delete(tableName, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (deletedContact != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deletedContact;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable
            String[] selectionArgs) {

        final SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int updatedContact;

        switch (sUriMatcher.match(uri)) {
            case CODE_SINGLE_CONTACT:
                String id = uri.getLastPathSegment();
                String tableName = DetectorContract.DetectorEntry.TABLE_NAME;

                updatedContact = database.update(tableName, values, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (updatedContact != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updatedContact;
    }

}
