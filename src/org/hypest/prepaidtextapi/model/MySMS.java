
package org.hypest.prepaidtextapi.model;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.android.internal.telephony.gsm.SmsMessage;

public class MySMS implements SMSDBAdapter.BasicId {
    public static final String CLASS_UNDETERMINED = "CLASS_UNDETERMINED";
    public static final String PREFIX = "S";

    public enum Sender {
        UNSUPPORTED_SENDER,
        S1234; // example of incoming SMS sender

        private String mSenderString;

        private Sender() {
            mSenderString = name().substring(1);
        }

        public String number() {
            return mSenderString;
        }
    }

    public enum Recipient {
        RNONE,
        R1234; // example of outgoing SMS recipient

        private String mRecipientString;

        private Recipient() {
            mRecipientString = name().substring(1);
        }

        public String number() {
            return mRecipientString;
        }
    }

    protected long mId;
    protected Sender mSender;
    protected long mTimestamp;
    protected String mParseClass;
    protected boolean mProcessed;
    protected boolean mIgnore;
    protected boolean mViewed;
    protected boolean mReportedMothership;
    protected boolean mMarkForReport;
    protected boolean mUserDontReport;
    protected boolean mTrashed;
    protected String mBodyText = "";

    public long getId() {
        return mId;
    }

    public Sender getSender() {
        return mSender;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getParseClass() {
        return mParseClass;
    }

    public boolean getProcessed() {
        return mProcessed;
    }

    public boolean getIgnore() {
        return mIgnore;
    }

    public boolean getViewed() {
        return mViewed;
    }

    public boolean getReportedMothership() {
        return mReportedMothership;
    }

    public boolean getMarkForReport() {
        return mMarkForReport;
    }

    public boolean getUserDontReport() {
        return mUserDontReport;
    }

    public boolean getTrashed() {
        return mTrashed;
    }

    public String getBodyText() {
        return mBodyText;
    }

    public void setParseClass(String parseClass) {
        mParseClass = parseClass;
    }

    public void setProcessed(boolean flag) {
        mProcessed = flag;
    }

    public void setReportedMothership(boolean flag) {
        mReportedMothership = flag;
    }

    public MySMS(
            long id,
            long timestamp,
            String bodyText,
            String originatingAddress,
            String parseClass,
            boolean processed,
            boolean ignore,
            boolean viewed,
            boolean reportedMothership,
            boolean markForReport,
            boolean userDontReport,
            boolean trashed) 
    {
        mId = id;
        mTimestamp = timestamp;
        mBodyText = bodyText;
        mSender = ourSender(originatingAddress);
        mParseClass = parseClass;
        mProcessed = processed;
        mIgnore = ignore;
        mViewed = viewed;
        mReportedMothership = reportedMothership;
        mMarkForReport = markForReport;
        mUserDontReport = userDontReport;
        mTrashed = trashed;
    }

    public MySMS(
            long timestamp,
            String bodyText,
            String originatingAddress)
    {
        this(-1, timestamp, bodyText, originatingAddress, CLASS_UNDETERMINED,
                false, false, false, false, false, false, false);
    }

    public String getReport(SMSDBAdapter smsAdapter) {
        List<PduSMS> pdus = smsAdapter.fetchPDUs(mId);

        String from = pdus.get(0).getSmsMessage().getOriginatingAddress();
        String txt = "From: " + from + "<br/>";

        for (PduSMS pdu : pdus) {
            SmsMessage sms = pdu.getSmsMessage();
            txt += "Body: " + sms.getMessageBody() + "<br/>" +
                    "Binary form: " + PduSMS.sms2PDUB64(sms) + "<br/>" +
                    "RowId: " + pdu.getId() + "<br/>";
        }

        txt += "myVerification: " + MD5Hash(txt);

        return txt;
    }

    public static String MD5Hash(String t) {
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "error";
        }

        m.update(t.getBytes(), 0, t.length());
        String md5str = new BigInteger(1, m.digest()).toString(16);
        return md5str;
    }

    public static Sender ourSender(SmsMessage sms) {
        return ourSender(sms.getOriginatingAddress());
    }

    public static Sender ourSender(String originatingAddress) {
        try {
            Sender sender = Sender.valueOf(PREFIX + originatingAddress.trim());
            return sender;
        } catch (IllegalArgumentException e) {
            // not one of our recognized senders
        }

        return Sender.UNSUPPORTED_SENDER;
    }
}
