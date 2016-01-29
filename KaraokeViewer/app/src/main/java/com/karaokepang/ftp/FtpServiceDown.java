package com.karaokepang.ftp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.karaokepang.Activity.MainActivity;
import com.karaokepang.Util.Logger;
import com.midisheetmusic.FileUri;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FtpServiceDown extends AsyncTask<Void, Void, Void> {

    private Activity activity;
    private ProgressDialog progressDialog;
    private ArrayList<String> ftpFiles = new ArrayList<>();
    private ArrayList<String> localFiles = new ArrayList<>();

    public FtpServiceDown(Activity activity, ArrayList<String> localFiles) {
        this.activity = activity;
        this.localFiles = localFiles;
    }

    private FTPClient init() {

        FTPClient client = null;

        // 계정 로그인
        try {
            client = new FTPClient();

            client.setControlEncoding("euc-kr");

            Logger.i("FTP Client Test Program");
            Logger.i("Start~~~~~~");

            // TEST서버에 접속, test서버 도메일 혹은 ip 주소입력.
            client.connect("192.168.0.12");
            Logger.i("Connected to test.com...........");

            // 응답코드가 비정상일 경우 종료함
            int reply = client.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                client.disconnect();
                Logger.i("FTP server refused connection");
            } else {
                Logger.i(client.getReplyString());

                // timeout을 설정
                client.setSoTimeout(10000);

                // 로그인
                client.login("android", "123456789");
                Logger.i("login success...");

                client.setFileType(FTP.BINARY_FILE_TYPE);

                //ftpFileList
                FTPFile[] ftpdirs = client.listFiles("/vpang_mid");
                for (int i = 0; i < ftpdirs.length; i++) {
                    ftpFiles.add(ftpdirs[i].getName());
                }
                Log.e("kkk_ftp", ftpFiles.toString());
                Log.e("kkk_local", localFiles.toString());
                if (!(ftpFiles.toString().equals(localFiles.toString())) || ftpFiles.size()==0 || localFiles.size()==0) {
                    Log.e("kkk","fuck");
                    for (int i = 0; i < ftpdirs.length; i++) {
                        FileOutputStream fileOutputStream = new FileOutputStream("/mnt/sdcard/vpang_mid/" + ftpdirs[i].getName());
                        boolean result = client.retrieveFile("/vpang_mid/" + ftpdirs[i].getName(), fileOutputStream);
                        Log.e("kkk", "ftp result = " + result);
                    }
                }

//                client.logout();
            }
        } catch (Exception e) {
            Logger.i("해당 ftp 로그인 실패하였습니다.");
            e.printStackTrace();
            System.exit(-1);
        } finally {
            if (client != null && client.isConnected()) {
                try {
                    client.disconnect();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return client;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(activity, "", "신곡 업데이트중 입니다", true);
    }

    @Override
    protected Void doInBackground(Void... params) {
        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/vpang/");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File dir2 = new File(Environment.getExternalStorageDirectory().getPath() + "/vpang_mid/");
        if (!dir2.exists()) {
            dir2.mkdirs();
        }

        init();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            ((MainActivity) activity).loadSdcardMidiFiles();
        }
    }
}
