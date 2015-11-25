package com.jikexueyuan.jikecontacts;


import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanlin on 2015/11/24.
 */
public class ContactManager {

    public static List<ContactBean> getContacts(Context context) {
        List<ContactBean> contacts = new ArrayList<ContactBean>();

        ContentResolver resolver = context.getContentResolver();

        //获取到所有的联系人
        Cursor cRawContact = resolver.query(RawContacts.CONTENT_URI,
                new String[]{RawContacts._ID},
                null,
                null,
                null);

        ContactBean contact;
        while (cRawContact.moveToNext()) {
            contact = new ContactBean();

            //获取到联系人的rawContactId
            long rawContactId = cRawContact.getLong(cRawContact.getColumnIndex(RawContacts._ID));
            contact.setRawContactId(rawContactId);

            //利用Id查询与联系人对应的data信息
            Cursor dataCursor = resolver.query(Data.CONTENT_URI,
                    null,
                    Data.RAW_CONTACT_ID + "=?",
                    new String[]{String.valueOf(rawContactId)},
                    null);

            while (dataCursor.moveToNext()) {
                String data1 = dataCursor.getString(dataCursor.getColumnIndex(Data.DATA1));
                String mimetype = dataCursor.getString(dataCursor.getColumnIndex(Data.MIMETYPE));

                if (mimetype.equals(StructuredName.CONTENT_ITEM_TYPE)) {//当mimetype是名称类，data1就表示是名称
                    contact.setName(data1);
                } else if ((mimetype.equals(Phone.CONTENT_ITEM_TYPE))) {//当mimetype是phone，data1就表示是电话号码
                    contact.setPhone(data1);
                }
            }

            contacts.add(contact);
            dataCursor.close();
        }

        cRawContact.close();
        return contacts;
    }

    //新增联系人
    public static void addContact(Context context, ContactBean contact) {
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues();
        Uri rawContactUri = resolver.insert(RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);

        ContentValues valuesData1 = new ContentValues();
        valuesData1.put(Data.RAW_CONTACT_ID, rawContactId);
        valuesData1.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        valuesData1.put(Phone.NUMBER, contact.getPhone());
        resolver.insert(Data.CONTENT_URI, valuesData1);

        ContentValues valuesData2 = new ContentValues();
        valuesData2.put(Data.RAW_CONTACT_ID, rawContactId);
        valuesData2.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        valuesData2.put(StructuredName.DISPLAY_NAME, contact.getName());
        resolver.insert(Data.CONTENT_URI, valuesData2);
    }

    //实现更新
    public static void updateContact(Context context, ContactBean contact) throws RemoteException,
            OperationApplicationException {
        ContentResolver resolver = context.getContentResolver();

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI).
                withSelection(Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?",
                        new String[]{String.valueOf(contact.getRawContactId()),
                                StructuredName.CONTENT_ITEM_TYPE})
                .withValue(StructuredName.DISPLAY_NAME, contact.getName())
                .build());

        ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI).
                withSelection(Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?",
                        new String[]{String.valueOf(contact.getRawContactId()),
                                Phone.CONTENT_ITEM_TYPE})
                .withValue(Phone.NUMBER, contact.getPhone())
                .build());
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }
}
