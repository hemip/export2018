package com.teraim.exporter;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.teraim.exporter.JSONify.JSON_Report;
import com.teraim.strand.Provyta;

public class ProvytaAdapter extends BaseAdapter {

	private LayoutInflater mLayoutInflater;
	private List<Provyta> pyList;
	private boolean[] isCheckedL;
	List<JSON_Report> jsonL;
	private Context myCtx;


	public ProvytaAdapter(Context context, List<Provyta> pyList,List<JSON_Report> jsonL) {
		mLayoutInflater = LayoutInflater.from(context);
		this.pyList=pyList;
		if (pyList!=null) {
			isCheckedL = new boolean[pyList.size()];
			int i =0;
			for (Provyta py: pyList) {

				isCheckedL[i++] = py.isLocked();
			}
		}
		this.jsonL=jsonL;
		myCtx = context;
	}

	public Context getContext() {
		return myCtx;
	}
	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d("Strand","GetView called for position "+position);
		final int pos = position;
		Provyta py = pyList.get(position);
		if(convertView==null)
			convertView = mLayoutInflater.inflate(R.layout.pylist_row, null);
		CheckBox	cb =((CheckBox)convertView.findViewById(R.id.export));
		cb.setChecked(py.isLocked());
		cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isCheckedL!=null) {
					Log.d("v","ischecked "+isChecked+" pos: "+pos);
					isCheckedL[pos] = isChecked;
				}

			}
		});
		((TextView)convertView.findViewById(R.id.pyName)).setText(py.getpyID());

		((TextView)convertView.findViewById(R.id.markedReady)).setText(py.isLocked()?"Ja":"Nej");
		//((TextView)convertView.findViewById(R.id.markedExported)).setText("_");
		TextView tomma = ((TextView)convertView.findViewById(R.id.tomma));
		tomma.setText(Integer.toString(jsonL.get(position).empty.size()));
		tomma.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String out = format(jsonL.get(pos).empty);
				AlertDialog.Builder builder = new AlertDialog.Builder(ProvytaAdapter.this.getContext());
				builder.setTitle("Variabler som inte angivits")
						.setMessage(out).setPositiveButton("Ok", null)
						.show();
				Log.d("v",jsonL.get(pos).json.toString());
			}

			private String format(List<String> empty) {
				String out ="";
				int rows = 4;
				int rc=0;
				for(String s:empty) {
					if(rc<rows) {
						rc++;
						out+=s+", ";
					} else {
						rc=0;
						out+=s+"\n";
					}
				}
				return out;
			}

		});

		return convertView;
	}

	public boolean[] getCheckedRows() {
		return isCheckedL;
	}

	@Override
	public int getCount() {
		Log.d("Strand","Getcount called");
		return pyList.size();
	}

	@Override
	public Object getItem(int position) {
		Log.d("Strand","GetItem called");
		return pyList.get(position);
	}

	@Override
	public long getItemId(int position) {
		Log.d("Strand","GetItemId called");
		return position;
	}



}
