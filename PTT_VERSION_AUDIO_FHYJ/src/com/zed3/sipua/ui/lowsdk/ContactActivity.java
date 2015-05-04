package com.zed3.sipua.ui.lowsdk;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.zed3.dialog.DialogUtil;
import com.zed3.location.MemoryMg;
import com.zed3.screenhome.BaseActivity;
import com.zed3.sipua.R;
import com.zed3.sipua.message.MessageDialogueActivity;
import com.zed3.sipua.ui.MainActivity;
import com.zed3.sipua.ui.contact.AddContactDialog;
import com.zed3.sipua.ui.contact.CompareTool;
import com.zed3.sipua.ui.contact.ContactUtil;
import com.zed3.sipua.welcome.DeviceInfo;
import com.zed3.toast.MyToast;
import com.zed3.utils.Tools;

public class ContactActivity extends BaseActivity implements OnClickListener,
		SearchView.OnQueryTextListener {
	private static final int DELETE_ALL = 1;
	private static final int ADD = 2;
	private static final int IMPORT = 3;
	// ImageButton backBtn,setBtn,addcontact;
	LinearLayout l_importcontact, l_addContact;
	ListView contact_lv;
	List<ContactPerson> adapterlist;
	ContactManager cm;
	private View mRootView;
	protected boolean needInportGroupList = true;
	SearchView searchView;
	ContactAdapter contactAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mRootView = getLayoutInflater().inflate(R.layout.contact_main, null);
		setContentView(mRootView);
		mRootView.setOnClickListener(this);
		contact_lv = (ListView) findViewById(R.id.contact);
		cm = new ContactManager(this);
		registerReceiver(freshBr, new IntentFilter("com.zed3.contactfresh"));
		// backBtn = (ImageButton)findViewById(R.id.back_button);
		// backBtn.setOnClickListener(this);
		// setBtn= (ImageButton)findViewById(R.id.set_button);
		// setBtn.setOnClickListener(this);
		// addcontact =(ImageButton)findViewById(R.id.addcontact);
		// addcontact.setOnClickListener(this);
		// addcontact.setOnLongClickListener(new OnLongClickListener() {
		//
		// @Override
		// public boolean onLongClick(View v) {
		// // TODO Auto-generated method stub
		// importGroupMember();
		// return false;
		// }
		// });
		l_addContact = (LinearLayout) findViewById(R.id.addcontact);
		l_importcontact = (LinearLayout) findViewById(R.id.importcontact);
		l_addContact.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ImageView tv = (ImageView) findViewById(R.id.t_add);
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// l_addContact.setBackgroundResource(R.color.red);
					tv.setBackgroundResource(R.color.btn_click_bg);
					// tv.setTextColor(Color.WHITE);
					tv.setImageResource(R.drawable.add_contact_after);
					break;
				case MotionEvent.ACTION_UP:
					tv.setBackgroundResource(R.color.whole_bg);
					// l_addContact.setBackgroundResource(R.color.font_color3);
					// tv.setTextColor(getResources().getColor(R.color.font_color3));
					tv.setImageResource(R.drawable.add_contact_before);
					break;
				}
				return false;
			}
		});
		l_addContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ContactActivity.this,
						AddContactDialog.class);
				Bundle extras = new Bundle();
				extras.putString(AddContactDialog.USER_NUMBER, "");
				// extras.putString(AddContactDialog.USER_NAME, name);
				extras.putInt(AddContactDialog.TYPE, AddContactDialog.SAVE);
				intent.putExtras(extras);
				startActivity(intent);
			}
		});
		l_importcontact.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				TextView tv = (TextView) findViewById(R.id.t_import);
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					tv.setBackgroundResource(R.color.btn_click_bg);
					// l_importcontact.setBackgroundResource(R.color.red);
					tv.setTextColor(Color.WHITE);
					break;
				case MotionEvent.ACTION_UP:
					tv.setBackgroundResource(R.color.whole_bg);
					// l_importcontact.setBackgroundResource(R.color.font_color3);
					tv.setTextColor(getResources()
							.getColor(R.color.font_color3));
					break;
				}
				return false;
			}
		});
		l_importcontact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				importGroupMember();
			}
		});
		adapterlist = CompareTool.getInstance().sortByDefault(
				cm.query(ContactManager.QUERY_TYPE_ALL));
		contactAdapter = new ContactAdapter(this, adapterlist);
		contact_lv.setAdapter(contactAdapter);

		searchView = (SearchView) findViewById(R.id.search_view);
		searchView.setOnQueryTextListener(this);
	}

	protected void importGroupMember() {
		// TODO Auto-generated method stub
		if (adapterlist != null) {
			int count = adapterlist.size();
			if (GroupListUtil.getGroupListsMap().size() > 0) {
				String message = getResources().getString(
						R.string.import_dialog_message);
				DialogUtil.showSelectDialog(
						ContactActivity.this,
						getResources().getString(
								R.string.import_pttGroup_members), message,
						getResources().getString(R.string.import_dialog_ok),
						new DialogUtil.DialogCallBack() {

							@Override
							public void onPositiveButtonClick() {
								// TODO Auto-generated method stub
								// GroupListUtil.addGroupList2Contacts();
								// needInportGroupList = false;
								// refreshList();
								new ImportGroupMembersTask().execute();
							}

							@Override
							public void onNegativeButtonClick() {
								// TODO Auto-generated method stub
							}
						});

			} else {
				DialogUtil.showCheckDialog(ContactActivity.this, getResources()
						.getString(R.string.import_pttGroup_members),
						getResources().getString(R.string.no_group_memebers),
						getResources().getString(R.string.ok));
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// contact_lv.setOnItemClickListener(new OnItemClickListener() {//delete
		// by hu 2014/1/22
		//
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
		// long arg3) {
		// //清空处理然后设置某一项
		// for(int i=0;i<arg0.getChildCount();i++){
		// View child = arg0.getChildAt(i);
		// child.setBackgroundColor(getResources().getColor(R.color.white_diy));
		// child.findViewById(R.id.contact_video).setVisibility(View.INVISIBLE);
		// }
		// arg1.setBackgroundColor(getResources().getColor(R.color.holo_blue_light));
		// arg1.findViewById(R.id.contact_video).setVisibility(View.VISIBLE);
		//
		// }
		// });
		contact_lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0,
					final View arg1, int arg2, long arg3) {
				// 保持一致，长按选中和单击选中保持一致
				// for(int i=0;i<arg0.getChildCount();i++){
				// View child = arg0.getChildAt(i);
				// //
				// child.setBackgroundColor(getResources().getColor(R.color.white_diy));
				// //
				// child.findViewById(R.id.contact_video).setVisibility(View.INVISIBLE);//delete
				// by hu 2014/2/12
				// }
				// arg1.setBackgroundColor(getResources().getColor(R.color.holo_blue_light));
				// //delete by hu 2014/2/12
				// arg1.findViewById(R.id.contact_video).setVisibility(View.VISIBLE);

				Builder d = new AlertDialog.Builder(ContactActivity.this)
						.setTitle(R.string.options_one).setItems(
								R.array.contact_longclick_1,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {

										/* User clicked so do some stuff */
										String[] items = getResources()
												.getStringArray(
														R.array.contact_longclick_1);
										TextView tv_contact_name = (TextView) (arg1
												.findViewById(R.id.contact_name));
										TextView tv_contact_num = (TextView) (arg1
												.findViewById(R.id.contact_num));
										String name = tv_contact_name.getText()
												.toString();
										final String num = tv_contact_num
												.getText().toString();
										switch (which) {
										case 0:// 编辑联系人
											LayoutInflater factory = LayoutInflater
													.from(ContactActivity.this);
											final View textEntryView = factory
													.inflate(
															R.layout.contact_dialog_modify,
															null);
											EditText et_name = (EditText) textEntryView
													.findViewById(R.id.contact_name_md);
											et_name.setText(name);
											et_name.setSelection(name.length()); // 定位光标位置
											et_name.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
													12) });
											EditText et_num = (EditText) textEntryView
													.findViewById(R.id.contact_num_md);
											et_num.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
													15) });
											et_num.setText(num);
											new AlertDialog.Builder(
													ContactActivity.this)
													.setTitle(
															R.string.change_contact)
													.setView(textEntryView)
													.setPositiveButton(
															getResources()
																	.getString(
																			R.string.ok),
															new DialogInterface.OnClickListener() {
																public void onClick(
																		DialogInterface dialog,
																		int whichButton) {
																	EditText et_name = (EditText) textEntryView
																			.findViewById(R.id.contact_name_md);
																	String name = et_name
																			.getText()
																			.toString();
																	EditText et_num = (EditText) textEntryView
																			.findViewById(R.id.contact_num_md);
																	String num1 = et_num
																			.getText()
																			.toString();
																	if (!TextUtils
																			.isEmpty(name)
																			&& !TextUtils
																					.isEmpty(num1)) {
																		// String
																		// regExs=
																		// "^[a-zA-Z0-9\u4E00-\u9FA5]*$";
																		// Pattern
																		// p =
																		// Pattern.compile(regExs);
																		// Matcher
																		// m =
																		// p.matcher(name);
																		// if(!m.find()){
																		// MyToast.showToast(true,
																		// getApplicationContext(),
																		// "名称中含有无效字符");
																		// return;
																		// }
																		if (TextUtils
																				.isEmpty(cm
																						.queryNameByNum(num1))) {
																			ContactPerson cp = new ContactPerson();
																			cp.setContact_name(name);
																			cp.setContact_num(num1);
																			cm.insertContact(cp);
																			cm.deleteContact(num);
																		} else if (num
																				.equalsIgnoreCase(num1)) {
																			cm.updateContact(
																					num,
																					MyContactDatabase.CONTACT_NAME,
																					name);
																		} else {
																			// cm.updateContact(num,
																			// MyContactDatabase.CONTACT_NAME,
																			// name);
																			Toast.makeText(
																					ContactActivity.this,
																					getResources()
																							.getString(
																									R.string.edit_failed),
																					Toast.LENGTH_SHORT)
																					.show();
																		}
																		refreshList();
																	}
																}
															})
													.setNegativeButton(
															getResources()
																	.getString(
																			R.string.cancel),
															new DialogInterface.OnClickListener() {
																public void onClick(
																		DialogInterface dialog,
																		int whichButton) {
																}
															}).show();
											break;
										case 1:
											new AlertDialog.Builder(
													ContactActivity.this)
													.setTitle(items[which])
													.setMessage(
															getResources()
																	.getString(
																			R.string.delete_dialog_message)
																	+ " "+name+" "
																	+ getResources()
																	.getString(
																			R.string.delete_dialog_message_1))
													.setPositiveButton(
															getResources()
																	.getString(
																			R.string.delete_ok),
															new DialogInterface.OnClickListener() {
																@Override
																public void onClick(
																		DialogInterface dialog,
																		int which) {
																	cm.deleteContact(num);
																	refreshList();
																}
															})
													.setNegativeButton(
															getResources()
																	.getString(
																			R.string.cancel),
															new DialogInterface.OnClickListener() {
																@Override
																public void onClick(
																		DialogInterface dialog,
																		int which) {
																}
															}).show();
											break;
										case 2:// 删除全部联系人
											if (adapterlist != null) {
												int count = adapterlist.size();
												if (count > 0) {
													String message = (count > 1 ? getResources()
															.getString(
																	R.string.is_delete_all_1)
															: getResources()
																	.getString(
																			R.string.is_delete_all_2))+" "+
															+ count+" "
															+ (count > 1 ? getResources()
																	.getString(
																			R.string.is_delete_all_3)
																	: getResources()
																			.getString(
																					R.string.is_delete_all_4))
															+(count > 1 ? getResources()
																	.getString(
																			R.string.is_delete_all_5)
																	: getResources()
																			.getString(
																					R.string.is_delete_all_6));
													DialogUtil
															.showSelectDialog(
																	ContactActivity.this,
																	getResources()
																			.getString(
																					R.string.delete_all_contact),
																	message,
																	getResources()
																			.getString(
																					R.string.delete),
																	new DialogUtil.DialogCallBack() {

																		@Override
																		public void onPositiveButtonClick() {
																			// TODO
																			// Auto-generated
																			// method
																			// stub
																			new DeleteAllContactsTask()
																					.execute();
																		}

																		@Override
																		public void onNegativeButtonClick() {
																			// TODO
																			// Auto-generated
																			// method
																			// stub
																		}
																	});

												} else {
													// DialogUtil.showCheckDialog(ContactActivity.this,
													// "删除全部记录", "不需要删除！",
													// "确定"); //never used
													// //delete by hu 2014/2/12
												}
											}
											break;
										}
									}
								});
				Dialog dialog = d.create();
				// dialog.setCanceledOnTouchOutside(false);
				// dialog.setOnCancelListener(new OnCancelListener() {//暂不调用
				// @Override
				// public void onCancel(DialogInterface dialog) {
				// refreshList();
				// }
				// });
				dialog.show();
				return false;
			}
		});
		contact_lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
			}
		});

	}

	private void refreshList() {
		// add by zdx 刷新列表时清空搜索框内容
		searchView.setIconified(true);
		adapterlist = CompareTool.getInstance().sortByDefault(
				cm.query(ContactManager.QUERY_TYPE_ALL));
		// contact_lv.setAdapter(new ContactAdapter(this,adapterlist));//需后期优化
		contactAdapter.contact_person = adapterlist;
		contact_lv.setAdapter(contactAdapter);
	}

	class ContactAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context context;
		private int left;
		private List<ContactPerson> contact_person;
		private String keyword = "";

		public ContactAdapter(Context ctx, List<ContactPerson> contact_person) {
			mInflater = LayoutInflater.from(ctx);
			context = ctx;
			this.contact_person = contact_person;
		}

		@Override
		public int getCount() {
			return contact_person.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.contact_item, null);
				holder = new ViewHolder();
				holder.contact_name = (TextView) convertView
						.findViewById(R.id.contact_name);
				holder.contact_num = (TextView) convertView
						.findViewById(R.id.contact_num);
				holder.audio_view = (ImageView) convertView
						.findViewById(R.id.call_voice_btn);
				holder.msg_view = (ImageView) convertView
						.findViewById(R.id.call_msg_btn2);
				holder.video_view = (ImageButton) convertView
						.findViewById(R.id.contact_video);
				holder.line_sub = (LinearLayout) convertView
						.findViewById(R.id.line_sub);
				holder.line_sub2 = (LinearLayout) convertView
						.findViewById(R.id.line_sub2);
				convertView.setTag(holder);
			} else {
				// convertView.setBackgroundColor(getResources().getColor(R.color.white_diy));
				// convertView.findViewById(R.id.contact_video).setVisibility(View.INVISIBLE);
				holder = (ViewHolder) convertView.getTag();
			}
			left = 0;
			if (!DeviceInfo.CONFIG_SUPPORT_VIDEO) {
				holder.line_sub.setVisibility(View.GONE);
				holder.video_view.setVisibility(View.GONE);
			} else {
				left++;
			}
			if (!DeviceInfo.CONFIG_SUPPORT_AUDIO) {
				holder.line_sub2.setVisibility(View.GONE);
				holder.audio_view.setVisibility(View.GONE);
			} else {
				left++;
			}

			if (!DeviceInfo.CONFIG_SUPPORT_IM) {
				holder.msg_view.setVisibility(View.GONE);
			} else {
				left++;
			}
			if (DeviceInfo.CONFIG_SUPPORT_VIDEO
					&& DeviceInfo.CONFIG_SUPPORT_AUDIO
					&& !DeviceInfo.CONFIG_SUPPORT_IM) {
				holder.line_sub2.setVisibility(View.GONE);
			}
			if (left == 1) {
				holder.line_sub.setVisibility(View.GONE);
				holder.line_sub2.setVisibility(View.GONE);
			}
			// 语音
			holder.audio_view.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String number = contact_person.get(position)
							.getContact_num();
					if (number == null) {
						DialogUtil.showCheckDialog(
								ContactActivity.this,
								getResources().getString(R.string.information),
								getResources().getString(
										R.string.number_not_exist),
								getResources().getString(R.string.ok_know));
					} else {
						// if (DeviceInfo.AudioType == 1)
						// CallUtil.makeAudioCall(ContactActivity.this, number,
						// null);
						// else {
						// Intent intent = new Intent(Intent.ACTION_CALL, Uri
						// .parse("tel:" + number));
						// startActivity(intent);
						// }
						if (MemoryMg.getInstance().PhoneType == -1) {// 自动
							if (DeviceInfo.CONFIG_AUDIO_MODE == 1)
								CallUtil.makeAudioCall(ContactActivity.this,
										number, null);
							else {
								Intent intent = new Intent(Intent.ACTION_CALL,
										Uri.parse("tel:" + number));
								startActivity(intent);
							}
						} else {// 手动
							if (MemoryMg.getInstance().PhoneType == 1)
								CallUtil.makeAudioCall(ContactActivity.this,
										number, null);
							else {
								Intent intent = new Intent(Intent.ACTION_CALL,
										Uri.parse("tel:" + number));
								startActivity(intent);
							}
						}

					}
				}
			});
			holder.msg_view.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(context,
							MessageDialogueActivity.class);
					intent.putExtra(MessageDialogueActivity.USER_NAME,
							contact_person.get(position).getContact_name());
					intent.putExtra(MessageDialogueActivity.USER_NUMBER,
							contact_person.get(position).getContact_num());

					startActivity(intent);
				}
			});

			convertView.findViewById(R.id.contact_video).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							CallUtil.makeVideoCall(ContactActivity.this,
									contact_person.get(position)
											.getContact_num(), null);
						}
					});
			// Bind the data efficiently with the holder.
			String name = contact_person.get(position).getContact_name();
			String num = contact_person.get(position).getContact_num();

			// holder.contact_name.setText(contact_person.get(position)
			// .getContact_name());
			// holder.contact_num.setText(contact_person.get(position)
			// .getContact_num());

			if (name != null
					&& name.toLowerCase().contains(keyword.toLowerCase())) {
				holder.contact_name.setText(getHighLightText(name, keyword));
			} else {
				holder.contact_name.setText(name);
			}
			if (num != null && num.contains(keyword)) {
				holder.contact_num.setText(getHighLightText(num, keyword));
			} else {
				holder.contact_num.setText(num);
			}
			registerForContextMenu(convertView);
			return convertView;
		}

		public CharSequence getHighLightText(String str, String keyword) {
			int index = str.toLowerCase().indexOf(keyword.toLowerCase());
			int len = keyword.length();
			Spanned temp = Html.fromHtml(str.substring(0, index)
					+ "<u><font color=#FF0000>"
					+ str.substring(index, index + len) + "</font></u>"
					+ str.substring(index + len, str.length()));
			return temp;
		}
	}

	class ViewHolder {
		TextView contact_name;
		TextView contact_num;
		ImageButton video_view;
		ImageView msg_view;
		ImageView audio_view;
		LinearLayout line_sub;
		LinearLayout line_sub2;
	}

	@Override
	public void onClick(View v) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Tools.exitApp(ContactActivity.this);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * ImportGroupMembersTask add by oumogang 2014-01-13
	 * 
	 * @author oumogang
	 */
	class ImportGroupMembersTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog processDailog;
		private int addCount;

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			DialogUtil.dismissProcessDailog(processDailog);
			needInportGroupList = false;
			refreshList();
			Toast.makeText(
					getApplicationContext(),
					getResources().getString(R.string.import_success_1)
							+ " "+addCount+" "
							+ (addCount<=1?getResources().getString(
									R.string.import_success_3):getResources().getString(
											R.string.import_success_2)), 0).show();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			processDailog = DialogUtil.showProcessDailog(ContactActivity.this,
					getResources().getString(R.string.importing_pttMembers));
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected Void doInBackground(Void... values) {
			addCount = GroupListUtil.addGroupList2Contacts();
			return null;
		}

	}

	/**
	 * DeleteAllContactsTask add by oumogang 2014-01-13
	 * 
	 * @author oumogang
	 */
	class DeleteAllContactsTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog processDailog;

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			DialogUtil.dismissProcessDailog(processDailog);
			refreshList();
			needInportGroupList = true;
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			processDailog = DialogUtil.showProcessDailog(ContactActivity.this,
					getResources().getString(R.string.deleting));
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected Void doInBackground(Void... values) {
			ContactManager cm = new ContactManager(ContactActivity.this);
			cm.deleteAllContacts();
			ContactUtil.deleteAll();
			return null;
		}

	}

	BroadcastReceiver freshBr = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			refreshList();
		}
	};

	@Override
	public boolean onQueryTextChange(String newText) {
		// Toast.makeText(this, "onQueryTextChange", Toast.LENGTH_SHORT).show();
		List<ContactPerson> persons;
		if (!TextUtils.isEmpty(newText)) {
			persons = cm.queryContactsByKeyword(newText);
		} else {
			persons = cm.query(ContactManager.QUERY_TYPE_ALL);
		}
		// 搜索后重新排序联系人列表 modify by liangzhang 2014-08-07
		contactAdapter.contact_person = CompareTool.getInstance()
				.sortByDefault(persons);
		contactAdapter.keyword = newText;
		contactAdapter.notifyDataSetChanged();
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	protected void onDestroy() {
		unregisterReceiver(freshBr);
		super.onDestroy();
	}
}
