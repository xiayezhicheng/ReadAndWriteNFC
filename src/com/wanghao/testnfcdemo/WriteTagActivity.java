package com.wanghao.testnfcdemo;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WriteTagActivity extends Activity{


	boolean mWriteMode = false;
	private NfcAdapter mNfcAdapter;
	private PendingIntent mNfcPendingIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tag_write);
		
		((Button) findViewById(R.id.btn_write)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mNfcAdapter = NfcAdapter.getDefaultAdapter(WriteTagActivity.this);
				mNfcPendingIntent = PendingIntent.getActivity(WriteTagActivity.this, 0,
				    new Intent(WriteTagActivity.this, WriteTagActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

				enableTagWriteMode();
				 
				new AlertDialog.Builder(WriteTagActivity.this).setTitle("请将标签靠近手机背部以写入")
				    .setOnCancelListener(new DialogInterface.OnCancelListener() {
				        @Override
				        public void onCancel(DialogInterface dialog) {
				            disableTagWriteMode();
				        }

				    }).create().show();		
			}
		});
	}
	
	private void enableTagWriteMode() {
	    mWriteMode = true;
	    IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
	    IntentFilter[] mWriteTagFilters = new IntentFilter[] { tagDetected };
	    mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);		
	}

	private void disableTagWriteMode() {
	    mWriteMode = false;
		mNfcAdapter.disableForegroundDispatch(this);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    // Tag writing mode
	    if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
	        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	        NdefRecord record = NdefRecord.createMime("text/plain", ((TextView)findViewById(R.id.value)).getText().toString().getBytes());
	        NdefMessage message = new NdefMessage(new NdefRecord[] { record });
	        if (writeTag(message, detectedTag)) {
	            Toast.makeText(this, "写入数据，成功！", Toast.LENGTH_LONG)
	                .show();
	            finish();
	        } 
	    }
	}

	/*
	* Writes an NdefMessage to a NFC tag
	*/
	boolean writeTag(NdefMessage message, Tag tag)
	{
		int size = message.toByteArray().length;

		try
		{

			Ndef ndef = Ndef.get(tag);
			if (ndef != null)
			{
				ndef.connect();

				if (!ndef.isWritable())
				{
					Toast.makeText(this, "NFC Tag是只读的！", Toast.LENGTH_LONG)
							.show();
					return false;

				}
				if (ndef.getMaxSize() < size)
				{
					Toast.makeText(this, "NFC Tag的空间不足！", Toast.LENGTH_LONG)
							.show();
					return false;
				}

				ndef.writeNdefMessage(message);
				Toast.makeText(this, "已成功写入数据！", Toast.LENGTH_SHORT).show();
				return true;

			}
			else
			{
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null)
				{
					try
					{
						format.connect();
						format.format(message);
						Toast.makeText(this, "已成功写入数据！", Toast.LENGTH_SHORT).show();
						return true;
						
					}
					catch (Exception e)
					{
						Toast.makeText(this, "写入NDEF格式数据失败！", Toast.LENGTH_LONG)
								.show();
						return false;
					}
				}
				else
				{
					Toast.makeText(this, "NFC标签不支持NDEF格式！", Toast.LENGTH_LONG)
							.show();
					return false;

				}
			}
		}
		catch (Exception e)
		{
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			return false;
		}

	}


}
