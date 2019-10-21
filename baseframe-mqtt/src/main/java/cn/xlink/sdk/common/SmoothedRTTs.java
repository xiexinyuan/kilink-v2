package cn.xlink.sdk.common;


import java.util.HashMap;

/**
 * Exponential weighted moving average，加权移动平均.
 * 这里用于算RTT（Round Trip Time）
 * <p>
 * Created by legendmohe on 2016/12/5.
 */

public class SmoothedRTTs {

    public static float DEFAULT_ALPHA = 0.8f;
    public static int DEFAULT_LEADING_RTT_NUM = 3;
    public static int DEFAULT_TIMER_ID = -1;

    private int mRTT;
    private int mLeadingRTTNum;
    private int mCount;
    private float mAlpha;
    private HashMap<Integer, Long> mTimeStampArray = new HashMap<>();

    public SmoothedRTTs() {
        mAlpha = DEFAULT_ALPHA;
        mLeadingRTTNum = DEFAULT_LEADING_RTT_NUM;
    }

    public SmoothedRTTs(float alpha) {
        mAlpha = alpha;
        mLeadingRTTNum = DEFAULT_LEADING_RTT_NUM;
    }

    public SmoothedRTTs(int leadingRTTNum, float alpha) {
        mLeadingRTTNum = leadingRTTNum;
        mAlpha = alpha;
    }

    //////////////////////////////////////////////////////////////////////

    public synchronized void resetTimer() {
        resetTimer(DEFAULT_TIMER_ID);
    }

    public synchronized void resetTimer(int id) {
        mTimeStampArray.put(id, System.currentTimeMillis());
    }

    public synchronized int markCurrentTime() {
        return markCurrentTime(DEFAULT_TIMER_ID);
    }

    public synchronized int markCurrentTime(int id) {
        Long prevTime = mTimeStampArray.get(id);
        if (prevTime == null) {
            return -1;
        }
        return addNext((int) (System.currentTimeMillis() - prevTime));
    }

    public int addNext(int nextRTT) {
        mCount++;
        // 加权滑动平均算法
        mRTT = (int) ((mAlpha * mRTT) + ((1 - mAlpha) * nextRTT));
        if (mCount > mLeadingRTTNum) {
            return mRTT;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    public synchronized void reset() {
        mRTT = 0;
        mCount = 0;
        mTimeStampArray.clear();
    }

    //////////////////////////////////////////////////////////////////////

    public void setRTT(int rtt) {
        mRTT = rtt;
    }

    public int getRTT() {
        if (mCount == 0) {
            return Integer.MAX_VALUE;
        } else {
            return mRTT;
        }
    }
}
