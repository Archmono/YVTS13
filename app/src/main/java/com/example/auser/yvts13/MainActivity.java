package com.example.auser.yvts13;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.auser.yvts13.data.MyDBHelper;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private MyDBHelper myDBHelper;
    private ListView listData;
    private EditText editName, editTel, editEmail, etSendMailText;
    String numberForDial;
    Boolean dialable = false;
    long rowID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.title);

        findViews();
        openDatabase();   //開啟資料庫
        showInList();    //show listview

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDatabase();
    }

    private void findViews() {
        editName = (EditText) findViewById(R.id.etName);
        editTel = (EditText) findViewById(R.id.etTel);
        editEmail = (EditText) findViewById(R.id.etEmail);
        listData = (ListView) findViewById(R.id.listView1);
    }

    public void clearETX(){
        editName.setText("");
        editTel.setText("");
        editEmail.setText("");
    }

    public void btnAdd(View v) {
        if(!editName.getText().toString().equals("") && !editName.getText().toString().equals("") && !editName.getText().toString().equals("")) {
            SQLiteDatabase db = myDBHelper.getWritableDatabase();
            ContentValues values = new ContentValues();  //建立 ContentValues 物件並呼叫 put(key,value) 儲存欲新增的資料，key 為欄位名稱  value 為對應值。
            values.put("name", editName.getText().toString());
            values.put("tel", editTel.getText().toString());
            values.put("email", editEmail.getText().toString());
            db.insert("tests", null, values);

            update();
            showInList();
            clearETX();
        } else {
            Toast.makeText(this, R.string.input_NULL, Toast.LENGTH_SHORT).show();
        }
    }

    public void btnEdit(View v) {
        update();
        showInList();
        clearETX();
    }

    public void btnDelete(View v) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        String id = Integer.toString((int) rowID);
        db.delete("tests", "_id=" + id, null);

        update();
        showInList();
        clearETX();
    }

    public void btnDial(View v) {
        if (dialable) {
            Intent call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + numberForDial));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivity(call);
        }else{
            Toast.makeText(this, "請先選擇一筆資料再撥打電話。", Toast.LENGTH_SHORT).show();
        }
    }

    public void btnSendMail(View v){
        LayoutInflater factory = LayoutInflater.from(MainActivity.this);
        final View v1 = factory.inflate(R.layout.mail,null);
        AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle(R.string.SendMSG);
        dialog.setView(v1);
        etSendMailText = (EditText) v1.findViewById(R.id.etMailInfo);
        Log.d("test",etSendMailText+"");
        dialog.setPositiveButton(R.string.Accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(numberForDial, null, etSendMailText.getText().toString(),
                        PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(), 0),
                        null);
            }

        });
        dialog.setNegativeButton(R.string.Cancel,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });
        dialog.show();
    }


    private void update(){
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        String id = Integer.toString((int) rowID);
        ContentValues values = new ContentValues();
        values.put("name",editName.getText().toString());
        values.put("tel",editTel.getText().toString());
        values.put("email",editEmail.getText().toString());
        db.update("tests",values, "_id=" + id,null);
    }

    private void openDatabase(){
        myDBHelper = new MyDBHelper(this);   //取得DBHelper物件

    }
    private void closeDatabase(){
        myDBHelper.close();
    }

    private Cursor getCursor(){
        SQLiteDatabase db = myDBHelper.getReadableDatabase();
        String[] columns={"_id", "name", "tel", "email"};
        Cursor cursor = db.query("tests",columns,null,null,null,null,null);
        return cursor;
    }

    private void showInList(){
        Cursor cursor = getCursor();
        String[] from = {"name","tel","email"};
        int[] to = {R.id.txtName,R.id.txtTel,R.id.txtEmail};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,R.layout.data_item,cursor,from,to); //SimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to)
        listData.setAdapter(adapter);
        listData.setOnItemClickListener(this);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        rowID = id;
        Toast.makeText(this, "第" + (position + 1) + "項", Toast.LENGTH_SHORT).show();

        SQLiteDatabase db = myDBHelper.getReadableDatabase();
        String[] columns={"name", "tel", "email"};
        Cursor cursor = db.query("tests",columns,"_id=?" ,new String[]{id+""},null,null,null);
        cursor.moveToFirst();
        editName.setText(cursor.getString(cursor.getColumnIndex("name")));
        editTel.setText(cursor.getString(cursor.getColumnIndex("tel")));
        editEmail.setText(cursor.getString(cursor.getColumnIndex("email")));

        numberForDial = cursor.getString(cursor.getColumnIndex("tel"));
        dialable = true;    //判定可以打電話
    }

    @Override
    public void onBackPressed() {  //返回鍵事件
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.CheckExit);
        builder.setMessage(R.string.CheckExitMSG);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton(R.string.Accept,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.setNegativeButton(R.string.Cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });
        builder.show();
    }

}
