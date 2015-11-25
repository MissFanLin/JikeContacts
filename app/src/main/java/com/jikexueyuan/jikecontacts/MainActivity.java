package com.jikexueyuan.jikecontacts;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView lvContacts;
    private ContactAdapter adapter;
    private List<ContactBean> contacts = new ArrayList<ContactBean>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvContacts = (ListView) findViewById(R.id.lvContacts);

        adapter = new ContactAdapter(this, contacts);
        lvContacts.setAdapter(adapter);

        findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();

                setContactsData();
            }
        });

        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ContactBean contact = adapter.getItem(position);
                userDialog(contact);
            }
        });

        setContactsData();

    }

    //封装设置数据的方法
    private void setContactsData() {
        List<ContactBean> contactData = ContactManager.getContacts(this);
        contacts.clear();
        contacts.addAll(contactData);
        adapter.notifyDataSetChanged();
    }


    //添加联系人
    private void showAddDialog() {
        View view = View.inflate(this, R.layout.dialog_add, null);

        final EditText et_name = (EditText) view.findViewById(R.id.etName);
        final EditText et_phone = (EditText) view.findViewById(R.id.etPhone);

        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.add_contact))
                .setView(view)
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if ("".equals(et_name.getText().toString()) || "".equals(et_phone.getText().toString())) {
                            Toast toast = Toast.makeText(MainActivity.this, getResources().getString(R.string.text_Null), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        } else {
                            ContactBean contact = new ContactBean();
                            contact.setName(et_name.getText().toString());
                            contact.setPhone(et_phone.getText().toString());

                            ContactManager.addContact(MainActivity.this, contact);
                            setContactsData();
                        }

                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .show();
    }

    //显示更新对话框
    private void showUpdataDialog(final ContactBean oldContact) {
        View view = View.inflate(this, R.layout.dialog_add, null);

        final EditText et_name = (EditText) view.findViewById(R.id.etName);
        final EditText et_phone = (EditText) view.findViewById(R.id.etPhone);

        et_name.setText(oldContact.getName());
        et_phone.setText(oldContact.getPhone());

        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.modify))
                .setView(view)
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContactBean contact = new ContactBean();
                        contact.setRawContactId(oldContact.getRawContactId());
                        contact.setName(et_name.getText().toString());
                        contact.setPhone(et_phone.getText().toString());

                        try {
                            ContactManager.updateContact(MainActivity.this, contact);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (OperationApplicationException e) {
                            e.printStackTrace();
                        }
                        setContactsData();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .show();
    }

    //提供用户操作的Dialog
    private void userDialog(final ContactBean contact) {
        new AlertDialog.Builder(this)
                .setItems(new String[]{getResources().getString(R.string.call_phone)
                                , getResources().getString(R.string.send_Message)
                                , getResources().getString(R.string.modify)},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        Intent intentCall = new Intent();
                                        intentCall.setAction(Intent.ACTION_CALL);
                                        intentCall.setData(Uri.parse("tel:" + contact.getPhone()));
                                        startActivity(intentCall);
                                        break;
                                    case 1:
                                        Intent intentSend = new Intent();
                                        intentSend.setAction(Intent.ACTION_SENDTO);
                                        intentSend.setData(Uri.parse("smsto://" + contact.getPhone()));
                                        startActivity(intentSend);
                                        break;
                                    case 2:
                                        showUpdataDialog(contact);
                                        setContactsData();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).show();

        setContactsData();
    }
}
