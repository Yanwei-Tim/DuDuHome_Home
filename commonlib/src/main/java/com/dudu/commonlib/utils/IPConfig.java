package com.dudu.commonlib.utils;

import android.content.Context;

import com.dudu.commonlib.R;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.File.FileUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jason on 2016/7/14.
 */
public class IPConfig {
    public final static String CONFIG_FILE_NAME = "ipConfig.xml";

    private final int READ_SDFILE = 1;
    private final int READ_RAWFILE = 2;

    private Context mContext;
    private String configPath = "";
    private static IPConfig mIPConfig;

    //obd通讯数据采集服务器
    private String mServerIP = "pro-dudu-obd.gz.1251739498.clb.myqcloud.com";
    private int mServerPort = 9998;
    private String mTestServerIP = "119.29.132.60";
    private int mTestServerPort = 9998;


    //流媒体服务器
    private int mTcpPort = 54320;//test
    private String mTestTcpIp = "182.254.227.45";
    private String mTcpIp = "pro-dudu-stream.gz.1251739498.clb.myqcloud.com";
    private String mTestServerRtmp = "rtmp://182.254.227.45/myapp/";
    private String mServerRtmp = "rtmp://pro-dudu-stream.gz.1251739498.clb.myqcloud.com/myapp/";


    //外部API服务器
    private String SERVER_ADDRESS = "http://pro-dudu-api.gz.1251739498.clb.myqcloud.com:8888/";
    private String TEST_SERVER_ADDRESS = "http://119.29.137.192:8888/";

    //推送服务器
    private String SOCKET_ADDRESS = "pro-dudu-event.gz.1251739498.clb.myqcloud.com";
    private String TEST_SOCKET_ADDRESS = "119.29.154.212";
    private String TEST_SOCKETPORT = "9999";
    private String SOCKET_PORT = "9999";


    private String configDirectory;

    private boolean isTest_Server = false;

    private int versionCode;

    public IPConfig() {
        this.mContext = CommonLib.getInstance().getContext();
    }

    public static IPConfig getInstance() {

        if (mIPConfig == null)
            mIPConfig = new IPConfig();
        return mIPConfig;
    }

    public void init() {
//        configDirectory = FileUtil.getSdPath() + "/dudu/config";
//        configPath = configDirectory + "/" + CONFIG_FILE_NAME;
//        versionCode = getVersionCode(mContext);
//
//        File configFile = new File(configPath);
        readDefault_config(READ_RAWFILE, null);
//        if (!configFile.exists()) {
//            copyDefault(mContext, configFile);
//            changeConfig();
//        } else {
//            readDefault_config(READ_RAWFILE, configFile);
//            checkNewVersion();
//        }

    }

    public String getServerIP() {
        return isTest_Server ? mTestServerIP : mServerIP;
    }

    public int getServerPort() {
        return isTest_Server ? mTestServerPort : mServerPort;
    }

    public String getSOCKET_PORT() {
        return isTest_Server ? TEST_SOCKETPORT : SOCKET_PORT;
    }

    public String getSOCKET_ADDRESS() {
        return isTest_Server ? TEST_SOCKET_ADDRESS : SOCKET_ADDRESS;
    }

    public String getSERVER_ADDRESS() {
        return isTest_Server ? TEST_SERVER_ADDRESS : SERVER_ADDRESS;
    }

    public String getmServerRtmp() {
        return isTest_Server ? mTestServerRtmp : mServerRtmp;
    }

    public String getmTcpIp() {
        return isTest_Server ? mTestTcpIp : mTcpIp;
    }

    public int getmTcpPort() {
        return mTcpPort;
    }

    public boolean isTest_Server() {
        return isTest_Server;
    }

    public void setTest_Server(boolean isTest_Server) {
        this.isTest_Server = isTest_Server;
    }

    private void checkNewVersion() {

        if (versionCode < getVersionCode(mContext)) {
            readDefault_config(READ_RAWFILE, null);
            changeConfig();
        }

    }

    private void readDefault_config(int readType, File configFile) {
        try {
            InputStream in = mContext.getResources().openRawResource(R.raw.ip_config);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xmlParser = factory.newPullParser();
            switch (readType) {
                case READ_RAWFILE:
                    xmlParser.setInput(in, "UTF-8");
                    break;
                case READ_SDFILE:
                    try {
                        xmlParser.setInput(new FileReader(configFile));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            String tagName = new String("");
            int eventType = xmlParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = xmlParser.getName();
                } else if (eventType == XmlPullParser.TEXT) {
                    switch (tagName) {
                        case "server_ip":
                            mServerIP = xmlParser.getText();
                            break;
                        case "server_port":
                            mServerPort = Integer.parseInt(xmlParser.getText());
                            break;
                        case "test_server_ip":
                            mTestServerIP = xmlParser.getText();
                            break;
                        case "test_server_port":
                            mTestServerPort = Integer.parseInt(xmlParser.getText());
                            break;

                        case "rtmp":
                            mServerRtmp = xmlParser.getText();
                            break;
                        case "test_rtmp":
                            mTestServerRtmp = xmlParser.getText();
                            break;
                        case "tcpIp":
                            mTcpIp = xmlParser.getText();
                            break;
                        case "test_tcpIp":
                            mTestTcpIp = xmlParser.getText();
                            break;
                        case "tcpPort":
                            mTcpPort = Integer.parseInt(xmlParser.getText());
                            break;
                        case "server_address":
                            SERVER_ADDRESS = xmlParser.getText();
                            break;
                        case "test_server_address":
                            TEST_SERVER_ADDRESS = xmlParser.getText();
                            break;
                        case "socket_address":
                            SOCKET_ADDRESS = xmlParser.getText();
                            break;
                        case "test_socket_address":
                            TEST_SOCKET_ADDRESS = xmlParser.getText();
                            break;
                        case "socket_port":
                            SOCKET_PORT = xmlParser.getText();
                            break;
                        case "test_socket_port":
                            TEST_SOCKETPORT = xmlParser.getText();
                            break;
                        case "is_test":
                            isTest_Server = xmlParser.getText().equals("1");
                            break;
                        case "version":
                            versionCode = Integer.parseInt(xmlParser.getText());
                            break;
                    }

                } else {
                    tagName = new String("");
                }
                try {
                    eventType = xmlParser.next();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

    }

    private void copyDefault(Context context, File configFile) {
        if (!FileUtil.isSdCard())
            return;
        File fileDir = new File(configDirectory);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        FileOutputStream fileOutput = null;
        try {
            fileOutput = new FileOutputStream(configFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (null != fileOutput) {
            InputStream in = null;
            try {
                in = context.getResources().openRawResource(R.raw.ip_config);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            // 拷贝数据
            if (null != in) {
                byte[] buf = new byte[1024];
                int length = buf.length;

                try {
                    while (-1 != (length = in.read(buf, 0, length))) {
                        fileOutput.write(buf, 0, length);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 关闭文件
            try {
                fileOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean changeConfig() {
        File configFile = new File(configPath);
        XmlSerializer xmlFile;
        try {
            configFile.delete();
            configFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            xmlFile = XmlPullParserFactory.newInstance().newSerializer();
            xmlFile.setOutput(new FileWriter(configFile));
            xmlFile.startDocument("UTF-8", false);

            // <config>
            xmlFile.startTag(null, "config");

            // <server_ip>
            xmlFile.startTag(null, "server_ip");
            xmlFile.text(mServerIP);
            xmlFile.endTag(null, "server_ip");

            // <server_port>
            xmlFile.startTag(null, "server_port");
            xmlFile.text(Integer.valueOf(mServerPort).toString());
            xmlFile.endTag(null, "server_port");

            // <test_server_ip>
            xmlFile.startTag(null, "test_server_ip");
            xmlFile.text(mTestServerIP);
            xmlFile.endTag(null, "test_server_ip");

            // <test_server_port>
            xmlFile.startTag(null, "test_server_port");
            xmlFile.text(Integer.valueOf(mTestServerPort).toString());
            xmlFile.endTag(null, "test_server_port");

            // <rtmp>
            xmlFile.startTag(null, "rtmp");
            xmlFile.text(mServerRtmp);
            xmlFile.endTag(null, "rtmp");

            // <test_rtmp>
            xmlFile.startTag(null, "test_rtmp");
            xmlFile.text(mTestServerRtmp);
            xmlFile.endTag(null, "test_rtmp");

            // <tcpIp>
            xmlFile.startTag(null, "tcpIp");
            xmlFile.text(mTcpIp);
            xmlFile.endTag(null, "tcpIp");

            // <test_tcpIp>
            xmlFile.startTag(null, "test_tcpIp");
            xmlFile.text(mTestTcpIp);
            xmlFile.endTag(null, "test_tcpIp");


            // <tcpPort>
            xmlFile.startTag(null, "tcpPort");
            xmlFile.text(Integer.valueOf(mTcpPort).toString());
            xmlFile.endTag(null, "tcpPort");

            // <server_address>
            xmlFile.startTag(null, "server_address");
            xmlFile.text(SERVER_ADDRESS);
            xmlFile.endTag(null, "server_address");

            // <test_server_address>
            xmlFile.startTag(null, "test_server_address");
            xmlFile.text(TEST_SERVER_ADDRESS);
            xmlFile.endTag(null, "test_server_address");

            // <socket_address>
            xmlFile.startTag(null, "socket_address");
            xmlFile.text(SOCKET_ADDRESS);
            xmlFile.endTag(null, "socket_address");

            // <test_socket_address>
            xmlFile.startTag(null, "test_socket_address");
            xmlFile.text(TEST_SOCKET_ADDRESS);
            xmlFile.endTag(null, "test_socket_address");

            // <socket_port>
            xmlFile.startTag(null, "socket_port");
            xmlFile.text(SOCKET_PORT);
            xmlFile.endTag(null, "socket_port");

            // <test_socket_port>
            xmlFile.startTag(null, "test_socket_port");
            xmlFile.text(TEST_SOCKETPORT);
            xmlFile.endTag(null, "test_socket_port");


            // is_test_server
            xmlFile.startTag(null, "is_test");
            xmlFile.text(Integer.valueOf(isTest_Server ? 1 : 0).toString());
            xmlFile.endTag(null, "is_test");

            xmlFile.startTag(null, "version");
            xmlFile.text(getVersionCode(mContext) + "");
            xmlFile.endTag(null, "version");

            xmlFile.endTag(null, "config");

            xmlFile.endDocument();
            xmlFile.flush();
            return true;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean changeConfig(boolean isTest_Server) {
        return changeConfig();
    }

    public boolean changeConfig(String ip, String testIp, int port, int testPort, boolean istest) {

        this.mServerIP = ip;
        this.mTestServerIP = testIp;
        this.mServerPort = port;
        this.mTestServerPort = testPort;
        this.isTest_Server = istest;

        return changeConfig();
    }

    private int getVersionCode(Context context) {
        return VersionTools.getAppVersionCode(CommonLib.getInstance().getContext());
    }
}
