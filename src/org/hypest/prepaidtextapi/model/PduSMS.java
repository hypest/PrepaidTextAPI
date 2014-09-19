
package org.hypest.prepaidtextapi.model;

import biz.source_code.base64Coder.Base64Coder;

import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.gsm.SmsMessage;

public class PduSMS implements SMSDBAdapter.BasicId {
    public static final long PADDING = 1000000l;
    public static final long FIVE_DAY_MILLIS = 432000000l;

    protected long mId;
    protected long mTimestamp;
    protected byte[] mPdu;
    protected String mPduB64;
    protected SmsMessage mSmsMessage;
    protected long mUniqueConcatRef;
    protected long mConcatRef;
    protected long mConcatIndex;
    protected long mConcatLength;
    protected boolean mIsConcat;
    protected long mArchiveId;

    public long getId() {
        return mId;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public byte[] getPdu() {
        return mPdu;
    }

    public String getPduB64() {
        return mPduB64;
    }

    public SmsMessage getSmsMessage() {
        return mSmsMessage;
    }

    public long getUniqueConcatRef() {
        return mUniqueConcatRef;
    }

    public long getConcatRef() {
        return mConcatRef;
    }

    public long getConcatIndex() {
        return mConcatIndex;
    }

    public long getConcatLength() {
        return mConcatLength;
    }

    public boolean getIsConcat() {
        return mIsConcat;
    }

    public long getArchiveId() {
        return mArchiveId;
    }

    public void setArchiveId(long id) {
        mArchiveId = id;
    }

    protected PduSMS(
            long id,
            byte[] pdu,
            long timestamp,
            long archiveId)
    {
        mId = id;
        mTimestamp = timestamp;
        mPdu = pdu;
        mSmsMessage = SmsMessage.createFromPdu(pdu);
        mPduB64 = sms2PDUB64(mSmsMessage);
        SmsHeader header = mSmsMessage.getUserDataHeader();
        mIsConcat = header != null;
        if (mIsConcat) {
            SmsHeader.ConcatRef concat = mSmsMessage.getUserDataHeader().concatRef;
            mConcatRef = concat.refNumber;
            mUniqueConcatRef = mTimestamp / FIVE_DAY_MILLIS * PADDING + mConcatRef;
            mConcatIndex = concat.seqNumber;
            mConcatLength = concat.msgCount;
        } else {
            mUniqueConcatRef = mTimestamp;
            mConcatLength = 1;
        }

        mArchiveId = archiveId;
    }

    public PduSMS(
            long id,
            String pduB64,
            long timestamp,
            long archiveId)
    {
        this(id, PDUB642sms(pduB64).getPdu(), timestamp, archiveId);
    }

    public PduSMS(byte[] pdu, long timestamp) {
        this(-1, pdu, timestamp, -1l);
    }

    protected static String sms2PDUB64(SmsMessage sms) {
        char[] chars = Base64Coder.encode(sms.getPdu());
        String pdu_b64 = new String(chars);
        return pdu_b64;
    }

    protected static SmsMessage PDUB642sms(String pduB64) {
        byte[] pdu = Base64Coder.decode(pduB64);
        return SmsMessage.createFromPdu(pdu);
    }

    public static PduSMS PDUB642PduSMS(String pduB64, long timestamp) {
        return new PduSMS(Base64Coder.decode(pduB64), timestamp);
    }
}
