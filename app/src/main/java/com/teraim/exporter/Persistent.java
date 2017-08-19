package com.teraim.exporter;

import android.os.Environment;
import android.util.Log;

import com.teraim.strand.Provyta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

public class Persistent {

	private final static String path = Environment.getExternalStorageDirectory().getPath();
	//Root for the data objects storing data per provyta.
	public final static String DATA_ROOT_DIR = path+"/strand/data/";
	public final static String EXPORT_ROOT_DIR = path+"/exported/";

	public static ArrayList<Provyta>loadAll() {
		ArrayList<Provyta> pys = new ArrayList<Provyta>();

		String pyName;
		File folder = new File(DATA_ROOT_DIR);
		File expF = new File(EXPORT_ROOT_DIR);
		try {
			expF.mkdirs();
			folder.mkdirs();
		} catch (Exception e) {
			e.printStackTrace();
			return pys;
		}
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles == null) {
			Log.e("Strand", "Did not find any files inside " + DATA_ROOT_DIR);
		}
		else {
			Log.e("Strand","Data root: "+DATA_ROOT_DIR);
			for (int i = 0; i < listOfFiles.length; i++)
			{
				Log.e("Strand","Loading "+listOfFiles[i].getName());
				Provyta py;
				if (listOfFiles[i].isFile())
				{

					pyName = listOfFiles[i].getName();
					py = onLoad(pyName);
					if (py!=null)
						pys.add(py);
					else
						Log.e("Strand","Failed to load file with name: "+pyName);
				}
			}
		}
		return pys;
	}


	public static Provyta onLoad (String pyID) {
		Log.d("Strand","Load");
		// Read from disk using FileInputStream
		FileInputStream f_in = null;
		try {
			f_in = new
					FileInputStream(DATA_ROOT_DIR+pyID);
		} catch (FileNotFoundException e) {
			//filenotfound occurs if object has not yet been persisted.
			Log.d("Strand","Kunde inte hitta provyta med pyID "+pyID);
			return null;
		}

		// Read object using ObjectInputStream
		ObjectInputStream obj_in = null;
		try {
			obj_in = new ObjectInputStream (f_in);
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Read an object
		Object obj = null;
		try {
			obj = obj_in.readObject();
		} catch (OptionalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				obj_in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		if (obj instanceof Provyta)
		{
			Provyta py = ((Provyta)obj);
			py.setSaved(true);
			return py;

		} else
			Log.e("Strand", "persisted object had wrong Serialization ID: "+Provyta.getSerialversionuid());

		return null;
	}

	public static void export(JSONify.JSON_Report json_report, Provyta py) throws FileNotFoundException {

		Log.d("Strand", "exporting " + py.getpyID());
		// Write to disk with FileOutputStream

		PrintWriter out = new PrintWriter(EXPORT_ROOT_DIR + py.getpyID() + ".json");
		out.println(json_report.json);
		out.close();


	}

}


