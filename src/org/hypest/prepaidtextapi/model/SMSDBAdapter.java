
package org.hypest.prepaidtextapi.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SMSDBAdapter {

    private static final String UNQ_REF = "UNQ_REF";
    private static final String ROWID = "rowid";
    private static final String IN = " IN ";
    private static final String EQUALS = " == ";
    private static final String IS_FALSE = " = 0 ";
    private static final String IS_TRUE = " = 1 ";
    private static final String AND = " AND ";
    private static final String TBL_SMS_NAME = SMS.class.getSimpleName();
    private static final String TBL_PDU_NAME = PDU.class.getSimpleName();

    // Database fields
    public enum SMS {
        TIMESTAMP("INTEGER"),
        PARSE_CLASS("TEXT"),
        PROCESSED("BOOLEAN"),
        IGNORE("BOOLEAN"),
        VIEWED("BOOLEAN"),
        REPORTED_MOTHERSHIP("BOOLEAN"),
        MARK_FOR_REPORT("BOOLEAN"),
        USER_DONT_REPORT("BOOLEAN"),
        TRASHED("BOOLEAN"),
        BODY_TEXT("TEXT"),
        SENDER("TEXT");

        public static ContentValues createContentValues(MySMS mySMS) {
            ContentValues values = new ContentValues();
            values.put(SMS.TIMESTAMP.toString(), mySMS.getTimestamp());
            values.put(SMS.PARSE_CLASS.toString(), mySMS.getParseClass());
            values.put(SMS.PROCESSED.toString(), mySMS.getProcessed() ? 1 : 0);
            values.put(SMS.IGNORE.toString(), mySMS.getIgnore() ? 1 : 0);
            values.put(SMS.VIEWED.toString(), mySMS.getViewed() ? 1 : 0);
            values.put(SMS.REPORTED_MOTHERSHIP.toString(), mySMS.getReportedMothership() ? 1 : 0);
            values.put(SMS.MARK_FOR_REPORT.toString(), mySMS.getMarkForReport() ? 1 : 0);
            values.put(SMS.USER_DONT_REPORT.toString(), mySMS.getUserDontReport() ? 1 : 0);
            values.put(SMS.TRASHED.toString(), mySMS.getTrashed() ? 1 : 0);
            values.put(SMS.BODY_TEXT.toString(), mySMS.getBodyText());
            values.put(SMS.SENDER.toString(), mySMS.getSender().number());
            return values;
        }

        public static MySMS cursorToSMS(Cursor cursor) {
            return new MySMS(
                    cursor.getLong(cursor.getColumnIndex(ROWID)),
                    cursor.getLong(cursor.getColumnIndex(SMS.TIMESTAMP.toString())),
                    cursor.getString(cursor.getColumnIndex(SMS.BODY_TEXT.toString())),
                    cursor.getString(cursor.getColumnIndex(SMS.SENDER.toString())),
                    cursor.getString(cursor.getColumnIndex(SMS.PARSE_CLASS.toString())),
                    1 == cursor.getShort(cursor.getColumnIndex(SMS.PROCESSED.toString())),
                    1 == cursor.getShort(cursor.getColumnIndex(SMS.IGNORE.toString())),
                    1 == cursor.getShort(cursor.getColumnIndex(SMS.VIEWED.toString())),
                    1 == cursor.getShort(cursor.getColumnIndex(SMS.REPORTED_MOTHERSHIP.toString())),
                    1 == cursor.getShort(cursor.getColumnIndex(SMS.MARK_FOR_REPORT.toString())),
                    1 == cursor.getShort(cursor.getColumnIndex(SMS.USER_DONT_REPORT.toString())),
                    1 == cursor.getShort(cursor.getColumnIndex(SMS.TRASHED.toString())));
        }

        private static String CREATION = "";

        static {
            for (SMS s : EnumSet.allOf(SMS.class)) {
                if (CREATION.length() > 0) {
                    CREATION += ", ";
                }
                CREATION += s.toString() + " " + s.getType();
            }
        }

        private String mType;

        private SMS(String t) {
            mType = t;
        }

        public String getType() {
            return mType;
        }

        public static final String SQL_CREATE() {
            return "CREATE TABLE " + SMS.class.getSimpleName() + " ("
                    + CREATION + ");";
        }
    }

    // Database fields
    public enum PDU {
        TIMESTAMP("INTEGER"),
        PDU_B64("TEXT"),
        CONCAT_REF("INTEGER"),
        CONCAT_LENGTH("INTEGER"),
        CONCAT_INDEX("INTEGER"),
        IS_CONCAT("BOOLEAN"),
        ARCHIVE_ID("INTEGER");

        public static ContentValues createContentValues(PduSMS sms) {
            ContentValues values = new ContentValues();
            values.put(PDU.TIMESTAMP.toString(), sms.getTimestamp());
            values.put(PDU.PDU_B64.toString(), sms.getPduB64());
            values.put(PDU.CONCAT_REF.toString(), sms.getConcatRef());
            values.put(PDU.CONCAT_LENGTH.toString(), sms.getConcatLength());
            values.put(PDU.CONCAT_INDEX.toString(), sms.getConcatIndex());
            values.put(PDU.IS_CONCAT.toString(), sms.getIsConcat());
            values.put(PDU.ARCHIVE_ID.toString(), sms.getArchiveId());
            return values;
        }

        public static PduSMS cursorToSMS(Cursor cursor) {
            return new PduSMS(
                    cursor.getLong(cursor.getColumnIndex(ROWID)),
                    cursor.getString(cursor.getColumnIndex(PDU.PDU_B64.toString())),
                    cursor.getLong(cursor.getColumnIndex(PDU.TIMESTAMP.toString())),
                    cursor.getLong(cursor.getColumnIndex(PDU.ARCHIVE_ID.toString())));
        }

        private static String CREATION = "";

        static {
            for (PDU s : EnumSet.allOf(PDU.class)) {
                if (CREATION.length() > 0) {
                    CREATION += ", ";
                }
                CREATION += s.toString() + " " + s.getType();
            }
        }

        private String mType;

        private PDU(String t) {
            mType = t;
        }

        public String getType() {
            return mType;
        }

        public static final String SQL_CREATE() {
            return "CREATE TABLE " + PDU.class.getSimpleName() + " ("
                    + CREATION + ");";
        }
    }

    private Context mContext;
    private SQLiteDatabase mDB;
    private SMSDBHelper mDBHelper;

    public static class SMSDBHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "SMSDB";

        SMSDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SMS.SQL_CREATE());
            db.execSQL(PDU.SQL_CREATE());
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        }
    }

    public SMSDBAdapter(Context context) {
        mContext = context;
    }

    @SuppressWarnings("deprecation")
    public SMSDBAdapter open() throws SQLException {
        mDBHelper = new SMSDBHelper(mContext);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    @SuppressWarnings("deprecation")
    public void close() {
        mDBHelper.close();
    }

    public long insert(MySMS mySMS)
    {
        return mDB.insert(TBL_SMS_NAME, null, SMS.createContentValues(mySMS));
    }

    public boolean update(MySMS mySMS) {
        return mDB.update(TBL_SMS_NAME,
                SMS.createContentValues(mySMS), ROWID + "=" + mySMS.getId(), null) > 0;
    }

    public long insert(PduSMS pduSMS)
    {
        return mDB.insert(TBL_PDU_NAME, null, PDU.createContentValues(pduSMS));
    }

    public boolean update(PduSMS pduSMS) {
        return mDB.update(TBL_PDU_NAME,
                PDU.createContentValues(pduSMS), ROWID + "=" + pduSMS.getId(), null) > 0;
    }

    public Cursor fetchAllSMSs() {
        Cursor c = mDB.query(SMS.class.getSimpleName(), new String[] {
                ROWID, "*"
        }, null, null, null, null,
                null);

        if (c != null)
            c.moveToPosition(0);
        return c;
    }

    protected List<MySMS> fetchSMSs(String simpleFilter, String sortFirst, int maxCount) {
        String limit = "";
        if (maxCount > 0) {
            limit = " LIMIT " + maxCount;
        }

        String sort = "";
        if (sortFirst != null) {
            sort = sortFirst + ", ";
        }

        Cursor c = mDB.query(SMS.class.getSimpleName(),
                new String[] {
                        ROWID, "*"
                },
                simpleFilter,
                null, null, null,
                sort + ROWID + " DESC" + limit);

        List<MySMS> smss = new ArrayList<MySMS>();

        if (c == null) {
            return smss;
        }

        c.moveToFirst();
        while (!c.isAfterLast()) {
            smss.add(SMS.cursorToSMS(c));
            c.moveToNext();
        }

        c.close();

        return smss;
    }

    protected int countSMSs(String simpleFilter) {
        Cursor c = mDB.query(SMS.class.getSimpleName(),
                new String[] {
                    "*"
                },
                simpleFilter,
                null, null, null, null);

        int cnt = 0;
        if (c != null) {
            cnt = c.getCount();
            c.close();
        }

        return cnt;
    }

    public List<MySMS> fetchUnmarkedUnreportedSMSs() {
        return fetchSMSs(
                SMS.REPORTED_MOTHERSHIP + IS_FALSE +
                        AND + SMS.PARSE_CLASS + EQUALS + '"' + MySMS.CLASS_UNDETERMINED + '"' +
                        AND + SMS.MARK_FOR_REPORT + IS_FALSE
                , null, 0);
    }

    public List<MySMS> fetchMarkedUnreportedSMSs() {
        return fetchSMSs(
                SMS.REPORTED_MOTHERSHIP + IS_FALSE +
                        AND + SMS.PARSE_CLASS + EQUALS + '"' + MySMS.CLASS_UNDETERMINED + '"' +
                        AND + SMS.MARK_FOR_REPORT + IS_TRUE
                , null, 0);
    }

    public boolean haveUnmarkedUnreportedSMSs() {
        Cursor c = mDB.query(SMS.class.getSimpleName(),
                new String[] {
                    ROWID
                },
                SMS.REPORTED_MOTHERSHIP + IS_FALSE +
                        AND + SMS.PARSE_CLASS + EQUALS + '"' + MySMS.CLASS_UNDETERMINED + '"' +
                        AND + SMS.MARK_FOR_REPORT + IS_FALSE,
                null, null, null, null);

        boolean have = c.getCount() > 0;
        c.close();
        return have;
    }

    public List<MySMS> fetchNewSMSs() {
        return fetchSMSs(SMS.PROCESSED + IS_FALSE, null, 0);
    }

    public List<MySMS> fetchRest(int maxCount) {
        return fetchSMSs(SMS.PARSE_CLASS + EQUALS + '"' + MySMS.CLASS_UNDETERMINED + '"',
                SMS.VIEWED + " ASC", maxCount);
    }

    public List<MySMS> fetchAllRest() {
        return fetchSMSs(SMS.PARSE_CLASS + EQUALS + '"' + MySMS.CLASS_UNDETERMINED + '"',
                SMS.VIEWED + " ASC", 0);
    }

    public int countAllRest() {
        return countSMSs(SMS.PARSE_CLASS + EQUALS + '"' + MySMS.CLASS_UNDETERMINED + '"');
    }

    public List<MySMS> fetchUnreadRest() {
        return fetchSMSs(SMS.PARSE_CLASS + EQUALS + '"' + MySMS.CLASS_UNDETERMINED + '"' + AND
                + SMS.VIEWED + IS_FALSE, null, 0);
    }

    public int countUnreadRest() {
        return countSMSs(SMS.PARSE_CLASS + EQUALS + '"' + MySMS.CLASS_UNDETERMINED + '"' + AND
                + SMS.VIEWED + IS_FALSE);
    }

    public List<PduSMS> fetchNewPDUs() {
        return fetchPDUs(PDU.ARCHIVE_ID + " == -1");
    }

    public List<PduSMS> fetchPDUs(long archiveId) {
        return fetchPDUs(PDU.ARCHIVE_ID + " == " + archiveId);
    }

    protected List<PduSMS> fetchPDUs(String simpleFilter) {
        Cursor c = mDB.query(PDU.class.getSimpleName(),
                new String[] {
                        ROWID,
                        "*, " + PDU.TIMESTAMP + "/" + PduSMS.FIVE_DAY_MILLIS + "*" + PduSMS.PADDING
                                + "+" + PDU.CONCAT_REF + " as " + UNQ_REF
                },
                simpleFilter,
                null, null, null,
                UNQ_REF + ", " + PDU.CONCAT_INDEX + ", " + PDU.TIMESTAMP);

        List<PduSMS> smss = new ArrayList<PduSMS>();

        if (c == null) {
            return smss;
        }

        c.moveToFirst();
        while (!c.isAfterLast()) {
            smss.add(PDU.cursorToSMS(c));
            c.moveToNext();
        }

        c.close();

        return smss;
    }

    public boolean setReportedMothership(List<MySMS> mySMSs, boolean value) {
        return setBooleanValue(TBL_SMS_NAME, SMS.REPORTED_MOTHERSHIP, mySMSs, value);
    }

    public boolean setMarkForReport(List<MySMS> mySMSs, boolean value) {
        return setBooleanValue(TBL_SMS_NAME, SMS.MARK_FOR_REPORT, mySMSs, value);
    }

    public boolean setUserDontReport(List<MySMS> mySMSs, boolean value) {
        return setBooleanValue(TBL_SMS_NAME, SMS.USER_DONT_REPORT, mySMSs, value);
    }

    public boolean setViewed(List<MySMS> mySMSs, boolean value) {
        return setBooleanValue(TBL_SMS_NAME, SMS.VIEWED, mySMSs, value);
    }

    public boolean setArchiveId(List<PduSMS> pduSMSs, long archiveId) {
        return setLongValue(TBL_PDU_NAME, PDU.ARCHIVE_ID, pduSMSs, archiveId);
    }

    public interface BasicId {
        long getId();
    }

    protected boolean setBooleanValue(String tableName, Enum<?> columnName,
            List<? extends BasicId> list, boolean value)
    {
        ContentValues v = new ContentValues();
        String in = "";
        String prefix = "";
        String prefix2 = ",";
        for (BasicId sms : list) {
            in += prefix + sms.getId();
            prefix = prefix2;
        }

        in = "(" + in + ")";
        v.put("" + columnName, value);
        return mDB.update(tableName, v, ROWID + IN + in, null) > 0;
    }

    protected boolean setLongValue(String tableName, Enum<?> columnName,
            List<? extends BasicId> list, long value)
    {
        ContentValues v = new ContentValues();
        String in = "";
        String prefix = "";
        String prefix2 = ",";
        for (BasicId sms : list) {
            in += prefix + sms.getId();
            prefix = prefix2;
        }

        in = "(" + in + ")";
        v.put("" + columnName, value);
        return mDB.update(tableName, v, ROWID + IN + in, null) > 0;
    }
}
