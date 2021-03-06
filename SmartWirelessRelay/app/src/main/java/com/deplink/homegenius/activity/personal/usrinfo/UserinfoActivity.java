package com.deplink.homegenius.activity.personal.usrinfo;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.util.WeakRefHandler;
import com.deplink.homegenius.util.bitmap.BitmapHandler;
import com.deplink.homegenius.view.dialog.DeleteDeviceDialog;
import com.deplink.homegenius.view.dialog.PictureSelectDialog;
import com.deplink.homegenius.view.dialog.SexSelectDialog;
import com.deplink.homegenius.view.imageview.CircleImageView;
import com.deplink.homegenius.view.toast.ToastSingleShow;
import com.deplink.homegenius.view.viewselector.TimeSelector;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.homegenius.UserInfoAlertBody;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;
import com.zxy.tiny.core.FileKit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class UserinfoActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "UserinfoActivity";
    private TextView textview_title;
    private FrameLayout image_back;
    private RelativeLayout layout_user_header_image;
    private RelativeLayout layout_update_user_nickname;
    private RelativeLayout layout_update_sex;
    private RelativeLayout layout_birthday;
    private CircleImageView user_head_portrait;
    private TextView textview_show_birthday;
    private TextView textview_show_sex;
    private TextView textview_show_nicknamke;
    private SDKManager manager;
    private EventCallback ec;
    private DeleteDeviceDialog connectLostDialog;
    private SexSelectDialog mSexDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);
        initViews();
        initDatas();
        initEvents();
    }

    private void initDatas() {
        textview_title.setText("个人信息");
        mSexDialog = new SexSelectDialog(this);
        DeplinkSDK.initSDK(getApplicationContext(), Perfence.SDK_APP_KEY);
        connectLostDialog = new DeleteDeviceDialog(UserinfoActivity.this);
        connectLostDialog.setSureBtnClickListener(new DeleteDeviceDialog.onSureBtnClickListener() {
            @Override
            public void onSureBtnClicked() {
                startActivity(new Intent(UserinfoActivity.this, LoginActivity.class));
            }
        });
        manager = DeplinkSDK.getSDKManager();
        ec = new EventCallback() {

            @Override
            public void onSuccess(SDKAction action) {

            }

            @Override
            public void onBindSuccess(SDKAction action, String devicekey) {


            }
            @Override
            public void onGetImageSuccess(SDKAction action, final Bitmap bm) {
                //保存到本地
                try {
                    Log.i(TAG, "onGetImageSuccess");
                    user_head_portrait.setImageBitmap(bm);
                    saveToSDCard(bm);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onGetUserInfouccess(String info) {
                super.onGetUserInfouccess(info);
                Gson gson = new Gson();
                if(!info.equalsIgnoreCase("[]")){
                    UserInfoAlertBody responseInfo = gson.fromJson(info, UserInfoAlertBody.class);
                    textview_show_nicknamke.setText(responseInfo.getNickname());
                    textview_show_sex.setText(responseInfo.getGender());
                    textview_show_birthday.setText(responseInfo.getBirthday());
                }
            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {

            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                connectLostDialog.show();
                connectLostDialog.setTitleText("账号异地登录");
                connectLostDialog.setContentText("当前账号已在其它设备上登录,是否重新登录");
            }
        };
    }

    private void saveToSDCard(Bitmap bitmap) {
        String path = this.getFilesDir().getAbsolutePath();
        path = path + File.separator + "userIcon" + "userIcon.png";
        File dest = new File(path);
        try {
            FileOutputStream out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isUserLogin;
    private boolean hasGetUserImage;
    private String userName;

    @Override
    protected void onResume() {
        super.onResume();
        manager.addEventCallback(ec);
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        if (isUserLogin) {
            hasGetUserImage = Perfence.getBooleanPerfence(AppConstant.USER.USER_GETIMAGE_FROM_SERVICE);
            if (!isOnActivityResult) {
                if (!hasGetUserImage) {
                    Perfence.setPerfence(AppConstant.USER.USER_GETIMAGE_FROM_SERVICE, true);
                    manager.getImage(Perfence.getPerfence(Perfence.PERFENCE_PHONE));
                } else {
                    setLocalImage(user_head_portrait);
                }
                userName = Perfence.getPerfence(Perfence.PERFENCE_PHONE);
                manager.getUserInfo(userName);
            }

        }
    }
    private boolean isOnActivityResult;
    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
        isOnActivityResult = false;
    }

    private void setLocalImage(CircleImageView user_head_portrait) {
        boolean isSdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);// 判断sdcard是否存在
        if (isSdCardExist) {
            String path = this.getFilesDir().getAbsolutePath();
            path = path + File.separator + "userIcon" + "userIcon.png";
            File file = new File(path);
            if (file.exists()) {
                Bitmap bm = BitmapFactory.decodeFile(file.getPath());
                // 将图片显示到ImageView中
                user_head_portrait.setImageBitmap(bm);
            } else {
                user_head_portrait.setImageDrawable(getResources().getDrawable(R.drawable.defaultavatar));
            }
        } else {
            ToastSingleShow.showText(this, "sd卡不存在！");
        }
    }

    private void initViews() {
        textview_title = findViewById(R.id.textview_title);
        textview_show_nicknamke = findViewById(R.id.textview_show_nicknamke);
        image_back = findViewById(R.id.image_back);
        layout_user_header_image = findViewById(R.id.layout_user_header_image);
        layout_update_user_nickname = findViewById(R.id.layout_update_user_nickname);
        layout_update_sex = findViewById(R.id.layout_update_sex);
        layout_birthday = findViewById(R.id.layout_birthday);
        user_head_portrait = findViewById(R.id.user_head_portrait);
        textview_show_birthday = findViewById(R.id.textview_show_birthday);
        textview_show_sex = findViewById(R.id.textview_show_sex);

    }

    private void initEvents() {
        image_back.setOnClickListener(this);
        layout_user_header_image.setOnClickListener(this);
        layout_update_user_nickname.setOnClickListener(this);
        layout_update_sex.setOnClickListener(this);
        layout_birthday.setOnClickListener(this);
    }

    /**
     * 拍照选择图片
     */
    private void chooseFromCamera() {
        //构建隐式Intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //调用系统相机
        startActivityForResult(intent, CAMERA_CODE);
    }

    private void showImagePopup() {
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Choose image");
        startActivityForResult(chooserIntent, 100);
    }

    /**
     * 拍照选取图片
     */
    private static final int CAMERA_CODE = 1;
    private static final int CROP_CODE = 3;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                onBackPressed();
                break;
            case R.id.layout_update_user_nickname:
                if(isUserLogin){
                    Intent intent = new Intent(this, UpdateNicknameActivity.class);
                    intent.putExtra("nickname", textview_show_nicknamke.getText().toString());
                    startActivity(intent);
                }else {
                    ToastSingleShow.showText(this,"未登录,登录后操作");
                }

                break;
            case R.id.layout_update_sex:
                if(isUserLogin){
                    mSexDialog.setmOnSexSelectClickListener(new SexSelectDialog.onSexSelectClickListener() {
                        @Override
                        public void onSexSelect(String selectMode) {
                            textview_show_sex.setText(selectMode);
                            UserInfoAlertBody body = new UserInfoAlertBody();
                            body.setGender(selectMode);
                            manager.alertUserInfo(userName, body);
                        }
                    });
                    mSexDialog.show();
                }else{
                    ToastSingleShow.showText(this,"未登录,登录后操作");
                }
                break;
            case R.id.layout_birthday:
                if(isUserLogin){
                    TimeSelector timeSelector = new TimeSelector(this, new TimeSelector.ResultHandler() {
                        @Override
                        public void handle(String time, Calendar selectedCalendar) {
                            textview_show_birthday.setText(time);
                            UserInfoAlertBody body = new UserInfoAlertBody();
                            body.setBirthday(time);
                            manager.alertUserInfo(userName, body);
                        }
                    }, "1900-1-1");
                    timeSelector.show();
                }else{
                    ToastSingleShow.showText(this,"未登录,登录后操作");
                }
                break;
            case R.id.layout_user_header_image:
                PictureSelectDialog dialog = new PictureSelectDialog(this);
                dialog.setmOnModeSelectClickListener(new PictureSelectDialog.onModeSelectClickListener() {
                    @Override
                    public void onModeSelect(String action) {
                        switch (action) {
                            case "picture":
                                //拍照选择
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        chooseFromCamera();
                                    }
                                });
                                break;
                            case "from_album":
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showImagePopup();
                                    }
                                });
                                break;
                        }
                    }
                });
                dialog.show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Perfence.setPerfence(AppConstant.USER.USER_GETIMAGE_FROM_SERVICE, false);
        isOnActivityResult = true;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CROP_CODE:
                    Log.i(TAG,"照片裁剪"+(data==null));
                    if (data == null) {
                        return;
                    } else {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            //获取到裁剪后的图像
                            Bitmap bm = extras.getParcelable("data");
                            File file = new File(path);
                            if (file.exists()) {
                                bm = BitmapFactory.decodeFile(file.getPath());
                                // 将图片显示到ImageView中
                                user_head_portrait.setImageBitmap(bm);
                            } else {
                                user_head_portrait.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
                            }
                            user_head_portrait.setImageBitmap(bm);
                        }
                        manager.uploadImage(path);
                    }
                    break;
                case CAMERA_CODE:
                    //用户点击了取消
                    if (data == null) {
                        return;
                    } else {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            //获得拍的照片
                            Bitmap bm = extras.getParcelable("data");
                            //将Bitmap转化为uri
                            //user_head_portrait.setImageBitmap(bm);
                            Uri uri = saveBitmap(bm, "temp");
                            //启动图像裁剪
                            startImageZoom(uri, bm);
                        }
                    }
                    break;
                case 100:
                    if (data == null) {
                        Toast.makeText(getApplicationContext(), "Unable to pick image", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Uri imageUri = data.getData();
                    // user_head_portrait.setImageURI(imageUri);
                    Bitmap photoBmp = null;
                    if (imageUri != null) {
                        try {
                            photoBmp = BitmapHandler.getBitmapFormUri(UserinfoActivity.this, imageUri);
                            user_head_portrait.setImageBitmap(photoBmp);
                        } catch (IOException ignored) {

                        }
                    }
                    final String imagePath = getRealPathFromURI(imageUri);
                    saveToSDCard(photoBmp);
                    //  String imagePathCompress = imagePath + "compressPic.jpg";
                    //使用tiny框架压缩图片
                    Tiny.getInstance().init(UserinfoActivity.this.getApplication());
                    Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
                    options.outfile = FileKit.getDefaultFileCompressDirectory() + "/tiny-useriamge.jpg";
                    Log.i(TAG, "options.outfile=" + options.outfile);
                    Tiny.getInstance().source(imagePath).asFile().withOptions(options).compress(new FileCallback() {
                        @Override
                        public void callback(boolean isSuccess, String outfile, Throwable t) {
                            //return the compressed file path
                            Log.i(TAG, "imagePath=" + imagePath + "   outfile=" + outfile);
                            manager.uploadImage(outfile);
                        }
                    });
                    break;

            }
        }

    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    /**
     * 通过Uri传递图像信息以供裁剪
     *
     * @param uri
     */
    private void startImageZoom(Uri uri, Bitmap bm) {
        //构建隐式Intent来启动裁剪程序
        Intent intent = new Intent("com.android.camera.action.CROP");
        //设置数据uri和类型为图片类型
        intent.setDataAndType(uri, "image/*");
        //显示View为可裁剪的
        intent.putExtra("crop", true);
        //裁剪的宽高的比例为1:1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", false);
        //输出图片的宽高均为150
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        //裁剪之后的数据是通过Intent返回
        intent.putExtra("return-data", true);
        intent.putExtra("uri", uri);
        startActivityForResult(intent, CROP_CODE);
    }
    private String path = "";

    /**
     * 将Bitmap写入SD卡中的一个文件中,并返回写入文件的Uri
     *
     * @param bm
     * @param dirPath
     * @return
     */
    private Uri saveBitmap(Bitmap bm, String dirPath) {
        Log.i(TAG,"保存照片:"+dirPath);
        //新建文件夹用于存放裁剪后的图片
        File tmpDir = new File(Environment.getExternalStorageDirectory() + "/" + dirPath);
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
        //新建文件存储裁剪后的图片
        File img = new File(tmpDir.getAbsolutePath() + "/avator.png");
        path = img.getPath();
        try {
            //打开文件输出流
            FileOutputStream fos = new FileOutputStream(img);
            //将bitmap压缩后写入输出流(参数依次为图片格式、图片质量和输出流)
            bm.compress(Bitmap.CompressFormat.PNG, 85, fos);
            //刷新输出流
            fos.flush();
            //关闭输出流
            fos.close();
            //返回File类型的Uri
            return Uri.fromFile(img);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
