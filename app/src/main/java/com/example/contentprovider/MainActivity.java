package com.example.contentprovider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final int PERMISSIONS_REQUEST_READ_SMS = 2;
    private static final int PICK_CONTACT_REQUEST = 3;

    private TextView messageTextView;
    private String selectedContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageTextView = findViewById(R.id.messageTextView);
        Button selectContactButton = findViewById(R.id.selectContactButton);

        selectContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission(Manifest.permission.READ_CONTACTS)) {
                    pickContact();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                }
            }
        });
    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickContact();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_READ_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Thực hiện nếu cần khi được cấp quyền
            }
        }
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                selectedContact = cursor.getString(phoneIndex);
                cursor.close();
                showMessages(selectedContact); // Chuyển đổi tham số
            }
        }
    }

    private void showMessages(String contactNumber) {
        StringBuilder messages = new StringBuilder();
        Cursor cursor = getContentResolver().query(Telephony.Sms.CONTENT_URI, null, Telephony.Sms.ADDRESS + " = ?", new String[]{contactNumber}, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                    String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                    messages.append("Từ: ").append(address).append("\nNội dung: ").append(body).append("\n\n");
                } while (cursor.moveToNext());
            } else {
                messages.append("Không có tin nhắn nào từ liên hệ này.");
            }
            cursor.close();
        } else {
            messages.append("Không thể truy cập tin nhắn.");
        }

        messageTextView.setText(messages.toString());
    }
}
