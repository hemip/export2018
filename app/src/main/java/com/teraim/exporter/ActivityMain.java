package com.teraim.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teraim.exporter.JSONify.JSON_Report;
import com.teraim.strand.Provyta;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE;


public class ActivityMain extends Activity {
	static final int REQUEST_PERMISSION_READ = 1;
	static final int REQUEST_PERMISSION_WRITE = 2;
	private ListView pyListV;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException (Thread thread, Throwable e)
			{
				Log.e("vortex","Uncaught Exception detected in thread {"+thread+"} Exce: "+ e);
				//e.printStackTrace();
				handleUncaughtException (thread, e);
			}
		});

		setContentView(R.layout.activity_main);
        try
        {
            String app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
			Log.e("vetrox", app_ver);
			if (app_ver!=null) {
				((TextView)findViewById(R.id.versionField)).setText("App version: "+app_ver);
			}
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Log.e("vetrox", e.getMessage());
        }

		getStarted();


	}

	private void handleUncaughtException(Thread thread, Throwable e) {

		e.printStackTrace(); // not all Android versions will print the stack trace automatically

		//invokeLogActivity();


		invokeLogActivity();


	}

	private void invokeLogActivity(){
		Intent intent = new Intent ();

			intent.putExtra("program_version", "1.02");
			intent.putExtra("app_name","Exporter");
			intent.putExtra("user_name","peter");
			intent.putExtra("team_name","rockabillies");

		intent.setAction ("com.teraim.exporter.SEND_LOG"); // see step 5.
		intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
		Log.d("vortex","Sending log file. Starting SendLog.");
		startActivity (intent);
		System.exit(1); // kill off the crashed app
	}

	private void exportMarkedRows(List<Provyta> pyL, List<JSON_Report> jsonL) {
		boolean[] checkedRowsA = ((ProvytaAdapter)pyListV.getAdapter()).getCheckedRows();
		int i=0; StringBuilder listB = new StringBuilder();
		for (boolean rowChecked:checkedRowsA) {
			if (rowChecked) {
				try {

					Persistent.export(jsonL.get(i),pyL.get(i));
					listB.append(pyL.get(i).getpyID());
					listB.append("\n");
				}

				catch (FileNotFoundException e) {
					Log.e("vortex","IO ERROR");
				}

			}
			i++;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Exporterade: ")
				.setMessage(listB.toString()).setPositiveButton("Ok", null)
				.show();
	}




	private void getStarted() {

		//Get provytor if any.
		final List<Provyta> pyL = Persistent.loadAll();
		final List<JSON_Report> jsonL = new ArrayList<JSON_Report>();
		Log.d("Strand", "pylist contains " + pyL.size() + " objects");

		//Generate JSON.
		JSONify jsonParser = new JSONify();

		//Parse all py into json
		try {
			for (Provyta py : pyL)
				if (py.isNormal()) {
					Log.d("Strand", "generating normal json for py " + py.getpyID());
					jsonL.add(jsonParser.normal(py));

				} else {
					Log.d("Strand", "generating json no_input for py " + py.getpyID());
					jsonL.add(jsonParser.noInput(py));

				}
		} catch (IOException e) {

			e.printStackTrace();
		}

		pyListV = (ListView) findViewById(R.id.list);
		//ArrayAdapter<Provyta> myDataAdapter = new ArrayAdapter<Provyta>(this,R.layout.pylist_row,R.id.pyName,pyList);

		pyListV.setAdapter(new ProvytaAdapter(this, pyL, jsonL));

		if (pyL.isEmpty()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Ingen mätdata")
					.setMessage("Programmet kunde inte hitta några påbörjade provytor.").setPositiveButton("Ok", null)
					.show();
		}

		Button exportButton = (Button)findViewById(R.id.button);

		exportButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				exportMarkedRows(pyL,jsonL);
			}
		});
		//Borttaget i version 2017.01
		//FTPSend f = new FTPSend();
		//f.send("test.txt", jsonL.get(0).json);
		//Log.d("Strand",jsonL.get(0).json);
		//new Thread(f).start();


	}


	private class FileSend implements Runnable {

		URL url;
		String toSend;

		public void send(String fileName,String toSend) {
			this.toSend=toSend;
			try {
				url = new URL("ftp://anonymous:anonymous@salix.slu.se/upload/strand/"+fileName);
			} catch (MalformedURLException e) {

				e.printStackTrace();
			}

		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

		}

	}




	private class FTPSend implements Runnable {

		URL url;
		String toSend;

		public void send(String fileName,String toSend) {
			this.toSend=toSend;
			try {
				url = new URL("ftp://anonymous:anonymous@salix.slu.se/upload/strand/"+fileName);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		@Override
		public void run() {

			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			URLConnection conn=null;

			try {
				conn = url.openConnection();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			OutputStream os=null;
			// to upload, open an OutputStream, to download, open an InputStream:  
			try {
				os = conn.getOutputStream();
				final PrintStream printStream = new PrintStream(os);
				printStream.print(toSend);
				printStream.close();
				Log.d("Strand","Printed to outstream: "+toSend);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

}
