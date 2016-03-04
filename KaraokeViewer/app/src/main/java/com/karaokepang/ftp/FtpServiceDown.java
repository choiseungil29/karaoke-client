package com.karaokepang.ftp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.karaokepang.Activity.TestActivity;
import com.karaokepang.Util.Logger;
import com.karaokepang.launcher.LauncherMainActivity;

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
            client.setConnectTimeout(3000);

            client.setControlEncoding("euc-kr");

            Logger.i("FTP Client Test Program");
            Logger.i("Start~~~~~~");

            //client.connect("192.168.0.13");
            client.connect("1.212.161.18");
            Logger.i("Connected to test.com...........");

            // 응답코드가 비정상일 경우 종료함
            int reply = client.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                client.disconnect();
                Logger.i("FTP server refused connection");
            } else {
                Logger.i(client.getReplyString());

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
                if (ftpFiles.size() != localFiles.size() || localFiles.size() == 0) {
                    Log.e("kkk", "fuck");
                    for (int i = 0; i < ftpdirs.length; i++) {
                        FileOutputStream fileOutputStream = new FileOutputStream("/mnt/sdcard/vpang_mid/" + ftpdirs[i].getName());
                        boolean result = client.retrieveFile("/vpang_mid/" + ftpdirs[i].getName(), fileOutputStream);
                        Log.e("kkk", "ftp result = " + result);
                    }
                }

                client.logout();
            }
        } catch (Exception e) {
            Logger.i("ftp 다운중 오류");
            e.printStackTrace();
            return null;
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
        initDefaultFileDirCheck();
        progressDialog = ProgressDialog.show(activity, "", "신곡 업데이트중 입니다", true);
    }

    @Override
    protected Void doInBackground(Void... params) {
        init();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            ((LauncherMainActivity) activity).loadSdcardMidiFiles();
        }
    }

    private void initDefaultFileDirCheck() {
        File dirVpang = new File("/mnt/sdcard/vpang");
        if (!dirVpang.exists()) {
            Log.i("kkk", "파일생성" + dirVpang.getAbsolutePath());
            dirVpang.mkdirs();
        }

        File dirVpangMid = new File("/mnt/sdcard/vpang_mid");
        if (!dirVpangMid.exists()) {
            Log.i("kkk", "파일생성" + dirVpangMid.getAbsolutePath());
            dirVpangMid.mkdirs();
        }
    }
}
