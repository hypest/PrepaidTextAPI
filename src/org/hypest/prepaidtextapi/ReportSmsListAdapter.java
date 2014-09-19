
package org.hypest.prepaidtextapi;

import java.util.Date;
import java.util.List;

import org.hypest.prepaidtextapi.model.MySMS;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import android.widget.TextView;

public class ReportSmsListAdapter implements ListAdapter {
    private DataSetObservable iDataSetObservable = new DataSetObservable();
    protected List<MySMS> iMySMSs;
    protected LayoutInflater iLi;
    protected OnClickListener iOnClickListener;
    protected OnCheckedChangeListener iOnCheckedChangeListener;

    public ReportSmsListAdapter(Context context, List<MySMS> mySMSs,
            OnClickListener onClickListener,
            OnCheckedChangeListener onCheckedChangeListener)
    {
        super();
        iMySMSs = mySMSs;
        iLi = LayoutInflater.from(context);
        iOnClickListener = onClickListener;
        iOnCheckedChangeListener = onCheckedChangeListener;
    }

    public void setData(List<MySMS> smss) {
        iMySMSs = smss;
    }

    public void refresh() {
        iDataSetObservable.notifyChanged();
    }

    @Override
    public int getCount() {
        return iMySMSs.size() + 1;
    }

    @Override
    public MySMS getItem(int position) {
        if (position == 0) {
            return null;
        } else {
            return iMySMSs.get(position - 1);
        }
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public View getView(final int position, View convertView,
            ViewGroup parent)
    {
        View v = convertView;

        if (convertView == null) {
            if (position == 0) {
                v = iLi.inflate(R.layout.sms_list_report_title, null);
            } else {
                v = iLi.inflate(R.layout.rest_sms_report, null);
            }
        }

        if (position == 0) {
            return v;
        }

        if (position - 1 > iMySMSs.size()) {
            return null;
        }

        MySMS sms = iMySMSs.get(position - 1);

        v.setOnClickListener(iOnClickListener);

        TextView rest_sms = (TextView) v.findViewById(R.id.single_rest_sms);
        rest_sms.setText(sms.getBodyText());

        TextView rest_sms_info = (TextView) v.findViewById(R.id.single_rest_sms_info);
        rest_sms_info.setText((new Date(sms.getTimestamp())).toLocaleString());

        View indicator = v.findViewById(R.id.indicator);
        if (sms.getViewed()) {
            indicator.setBackgroundResource(R.color.sms_viewed);
        } else {
            indicator.setBackgroundResource(R.color.sms_unviewed);
        }

        CheckBox cb = (CheckBox) v.findViewById(R.id.checkBox);
        cb.setTag(sms);
        cb.setChecked(!sms.getUserDontReport());
        cb.setOnCheckedChangeListener(iOnCheckedChangeListener);

        return v;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer)
    {
        iDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer)
    {
        iDataSetObservable.unregisterObserver(observer);
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return false;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return false;
    }
}
