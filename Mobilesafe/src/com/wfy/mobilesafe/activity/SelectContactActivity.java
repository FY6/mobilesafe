package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wfy.domain.Contact;
import com.wfy.mobilesafe.R;

import java.util.ArrayList;
import java.util.List;

public class SelectContactActivity extends Activity {
	private static final String PHONE_V2 = "vnd.android.cursor.item/phone_v2";
	private static final String NAME_V2 = "vnd.android.cursor.item/name";
	private ListView lv;
	private List<Contact> contacts = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact);
		lv = (ListView) findViewById(R.id.lv);

		getData();

		lv.setAdapter(new MyAdapter());
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String phone = contacts.get(position).getPhone();
				Intent data = new Intent();
				data.putExtra("phone", phone);
				
				setResult(Activity.RESULT_OK, data);
				
				finish();
			}
		});
	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return contacts.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(SelectContactActivity.this,
						R.layout.contact_item, null);
			}
			TextView tv_name = (TextView) convertView
					.findViewById(R.id.tv_name);
			TextView tv_phone = (TextView) convertView
					.findViewById(R.id.tv_phone);
			tv_name.setText(contacts.get(position).getName());
			tv_phone.setText(contacts.get(position).getPhone());
			return convertView;
		}

	}

	private void getData() {
		Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
		Cursor cursorContactId = getContentResolver().query(uri,
				new String[] { "contact_id" }, null, null, null);

		if (cursorContactId != null) {
			while (cursorContactId.moveToNext()) {
				String contact_id = cursorContactId.getString(cursorContactId
						.getColumnIndex("contact_id"));
				Cursor cursor = getContentResolver()
						.query(Uri.parse("content://com.android.contacts/data"),
								new String[] { "mimetype", "data1" },
								"raw_contact_id = ?",
								new String[] { contact_id }, null);

				String name = "";
				String phone = "";
				Contact contact = new Contact();
				while (cursor.moveToNext()) {

					if (NAME_V2.equals(cursor.getString(cursor
							.getColumnIndex("mimetype")))) {
						name = cursor.getString(cursor.getColumnIndex("data1"));
						contact.setName(name);
					} else if (PHONE_V2.equals(cursor.getString(cursor
							.getColumnIndex("mimetype")))) {
						phone = cursor
								.getString(cursor.getColumnIndex("data1"));
						contact.setPhone(phone);
					}
				}
				contacts.add(contact);
			}
		}
	}
}
