package com.datenkrake;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @author Thomas
 *
 */
public class UploadTask extends AsyncTask<String, Void, Integer>{

	private final String TAG = getClass().getSimpleName();
	URL url;
	private Util util;
	private Context mContext;
	
	public UploadTask (Context context) {
		mContext = context;
		util = new Util();
	}

	/**
	 * 
	 * @param filename Provide filename e.g. "/commFlows.csv"
	 * @return 
	 */
	public int upload(String filename) {

		File sourceFile = new File("data/data/com.datenkrake/files" + filename);

		String serverResponseMessage = null;
		int serverResponseCode = 0;
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;

		if (!sourceFile.isFile()) {
			Log.e("uploadFile", "Source File does not exist");
		} else {
			try {
				url = new URL("http://130.83.245.98:8015/dataReceiver.php");

				// open a URL connection to the Servlet
				FileInputStream fileInputStream = new FileInputStream(
						sourceFile);
				// Open a HTTP connection to the URL
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("uploaded_file", filename);

				dos = new DataOutputStream(conn.getOutputStream());

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
						+ filename + "\"" + lineEnd);

				dos.writeBytes(lineEnd);
				// create a buffer of maximum size
				bytesAvailable = fileInputStream.available();

				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				while (bytesRead > 0) {

					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				}

				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				serverResponseCode = conn.getResponseCode();
				serverResponseMessage = conn.getResponseMessage();

				Log.i(TAG, "HTTP Response is: "
						+ serverResponseMessage + ": " + serverResponseCode);

				if (serverResponseCode == 200) {
					String msg = "File Upload of " + filename + " Completed";
					Log.i(TAG, msg);
				}

				// close the streams //
				fileInputStream.close();
				dos.flush();
				dos.close();

			} catch (MalformedURLException ex) {

				Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
			} catch (Exception e) {

				Log.e("Upload file to server Exception",
						"Exception : " + e.getMessage(), e);
			}
		}
		return serverResponseCode;
	}

	@Override
	protected Integer doInBackground(String... params) {
		int result = upload(params[0]);
		if (result == 200) {
			util.deleteFile(params[0]);
		}
		return result;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		if (result == 200) {
			Toast.makeText(mContext, "Datenupload erfolgreich!", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(mContext, "Datenupload nicht erfolgreich!", Toast.LENGTH_SHORT).show();
		}
		util.refreshCsvs(mContext);
    }
}
