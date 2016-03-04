package com.karaokepang.ftp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.karaokepang.Util.Logger;
import com.karaokepang.launcher.LauncherMainActivity;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FtpServiceUp extends AsyncTask<Void, Void, Void> {

    private Activity activity;
    private ProgressDialog progressDialog;
    private String fileName;

    public FtpServiceUp(Activity activity, String fileName) {
        this.activity = activity;
        this.fileName = fileName;
        Log.e("kkk", "fileName =" + fileName);
    }

    private FTPClient init() {

        FTPClient client = null;

        // 계정 로그인
        try {
            client = new FTPClient();

            client.setControlEncoding("euc-kr");

            Logger.i("FTP Client Test Program");
            Logger.i("Start~~~~~~");

            //client.connect("192.168.0.13");
            client.connect("1.212.161.18");
            Logger.i("Connected to test.com...........");

            // 응답코드가 비정상일 경우 종료함
            int reply = client.getReplyCode();
            Log.e("kkk","reply = "+reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                client.disconnect();
                Logger.i("FTP server refused connection");
            } else {
                Logger.i(client.getReplyString());

                // timeout을 설정
                client.setSoTimeout(10000000);

                // 로그인
                client.login("android", "123456789");
                Logger.i("login success...");

                client.setFileType(FTP.BINARY_FILE_TYPE);

                client.cwd("/"); // ftp 상의 업로드 디렉토리
                client.mkd("vpang_video"); // public아래로 files 디렉토리를 만든다
                client.cwd("vpang_video"); // public/files 로 이동 (이 디렉토리로 업로드가 진행)

                File file = new File("/mnt/sdcard/vpang/" + fileName + ".mp4"); // 업로드 할 파일이 있는 경로(예제는 sd카드 사진 폴더)
                if (file.isFile()) {
                    Log.e("kkk", "============================");
                    Log.e("kkk", file.getAbsolutePath());
                    FileInputStream ifile = new FileInputStream(file);
                    client.rest(file.getName());  // ftp에 해당 파일이있다면 이어쓰기
//                    client.appendFile(file.getName(), ifile); // ftp 해당 파일이 없다면 새로쓰기
                    client.storeFile(file.getName(),ifile);
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
        progressDialog = ProgressDialog.show(activity, "", "영상 전송중 입니다", true);
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
            //((LauncherMainActivity) activity).loadSdcardMidiFiles();
        }
//        activity.finish();
    }
}
