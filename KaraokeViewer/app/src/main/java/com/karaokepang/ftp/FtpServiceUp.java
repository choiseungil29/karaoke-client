package com.karaokepang.ftp;

import android.os.AsyncTask;
import android.util.Log;

import com.karaokepang.Util.FilePath;
import com.karaokepang.Util.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FtpServiceUp extends AsyncTask<Void, Void, Void> {

    private String fileName;

    public FtpServiceUp(String fileName) {
        this.fileName = fileName;
    }

    private FTPClient init() {

        FTPClient client = null;

        Logger.i("======FTP Upload 진입===============");
        try {
            client = new FTPClient();

            client.setControlEncoding("euc-kr");

            Logger.i("======FTP Upload 시작===============");
            Logger.i("Start~~~~~~");

            //client.connect("192.168.0.13");
            client.connect("mediapot.iptime.org");
            Logger.i("Connected to test.com...........");

            // 응답코드가 비정상일 경우 종료함
            int reply = client.getReplyCode();
            Log.e("kkk", "reply = " + reply);
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

                File file = new File(FilePath.FILE_PATH_VPANG + fileName + ".mp4"); // 업로드 할 파일이 있는 경로(예제는 sd카드 사진 폴더)
                if (file.isFile()) {
                    Log.e("kkk", "============================");
                    Log.e("kkk", file.getAbsolutePath());
                    FileInputStream ifile = new FileInputStream(file);
                    client.rest(file.getName());  // ftp에 해당 파일이있다면 이어쓰기
//                    client.appendFile(file.getName(), ifile); // ftp 해당 파일이 없다면 새로쓰기
                    client.storeFile(file.getName(), ifile);
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
        Log.e("kkk", "ftp upload start");
    }

    @Override
    protected Void doInBackground(Void... params) {
        init();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d("kkk", "영상 전송이 완료되었습니다");
        try {
            File file = new File(FilePath.FILE_PATH_VPANG + fileName + ".mp4");
            boolean delete = file.delete();
            Log.d("kkk", file.getAbsoluteFile() + "삭제 완료 " + delete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
