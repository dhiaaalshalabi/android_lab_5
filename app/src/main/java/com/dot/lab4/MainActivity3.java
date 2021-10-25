package com.dot.lab4;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.dot.lab4.databinding.ActivityMain3Binding;
import com.google.android.material.button.MaterialButton;

public class MainActivity3 extends AppCompatActivity {
    ActivityMain3Binding binding;
    TextView btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        Log.d("TAG", data.toString());
                        contactPicked(data);
                    }
                }
        );
        btn = findViewById(R.id.btn_intent_action);
        binding.btnIntentAction.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"));
            startActivity(intent);
        });
        binding.btnIntentType.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(MainActivity3.this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity3.this, new String[]{Manifest.permission.READ_CONTACTS}, 1);

            } else {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
//            startActivity(intent);
                someActivityResultLauncher.launch(intent);
            }
        });
        binding.btnNotify.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity3.this, NotifyView.class);
            intent.putExtra("notificationId", 1);
            PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity3.this, 0, intent, 0);
            NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("My notification")
                    .setContentText("Hello World!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .addAction(R.drawable.ic_launcher_foreground, "Clear",
                            pendingIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                String channelId = "Your_channel_id";
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
                builder.setChannelId(channelId);
            }
            manager.notify(1,builder.build());
        });
    }

    private void contactPicked(Intent data) {
        Uri contactData = data.getData();
        Log.i("TAG ", "contactPicked() uri " + contactData.toString());
        Cursor c = managedQuery(contactData, null, null, null, null);
        if (c.moveToFirst()) {
            String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

            String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if (hasPhone.equalsIgnoreCase("1")) {
                Cursor phones = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                        null, null);
                phones.moveToFirst();
                String cNumber = phones.getString(phones.getColumnIndex("data1"));
                Log.d("TAG ", "number is:" + cNumber);
                phones.close();
            }
            String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            Log.d("TAG ", "name is:" + name);
            c.close();
        }
    }
}