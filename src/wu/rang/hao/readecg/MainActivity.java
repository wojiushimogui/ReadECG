package wu.rang.hao.readecg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private Button buttonRead;
	
	private SurfaceView sfvECG;
	
	private BTReadThread mReadThread = new BTReadThread(50);//�߳�ʵ����
	private Handler msgHandler;
	private DrawECGWaveForm mECGWF;
	BufferedReader bufferReader;
	StringBuffer strBuffer;
	File file;
	String str=new String();
	Boolean enRead=false;
	
	private String revTmpStr = new String();
	
	public List<Float> ECGDataList = new ArrayList<Float>();
	public boolean ECGDLIsAvailable = true;
	private float ECGData = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		buttonRead=(Button)findViewById(R.id.read);
		sfvECG=(SurfaceView)findViewById(R.id.sfvECG);
		mECGWF = new DrawECGWaveForm(sfvECG);//DrawECGWaveForm�����ʵ����������Ĳ���ΪSrufaceView��ʵ��
	
		Looper lp = Looper.myLooper();
		msgHandler = new MsgHandler(lp);//ʵ����һ��Handler����
		
		// Setting Timer to Draw and Save data
		Timer mDSTimer = new Timer();
		TimerTask mDSTask = new TimerTask(){
			public void run(){
				Message msg = Message.obtain();
				msg.what = 1;
				msgHandler.sendMessage(msg);
			}
		};
		
		// Set Timer
		mDSTimer.schedule(mDSTask,1000,1000);
		buttonRead.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				enRead=true;
				//writeDataToSD();
				mReadThread.start();
				
				
			}
		});
	}

	
	
	// MsgHandler class to Update UI
		class MsgHandler extends Handler{
			public MsgHandler(Looper lp){
				super(lp);
			}
			
			@Override
			public void handleMessage(Message msg){
				switch(msg.what){
				case 1:
					if (ECGDataList.size() > 1){
						List<Float> ECGCacheData = new ArrayList<Float>();
						ECGCacheData.addAll(ECGDataList);
						ECGDLIsAvailable = false;
						ECGDataList.clear();
						ECGDLIsAvailable = true;
						// Draw To View
						mECGWF.DrawtoView(ECGCacheData);	//����DrawECGWaveForm���е�DrawtoView�����Խ��ܵ������ݻ�ͼ��ʾ				
					}
					break;
				}

			}
		}
		
		public void writeDataToSD(){  
			try{  
			    /* ��ȡFile����ȷ�������ļ�����Ϣ */  
			    //File file  = new File(Environment.getExternalStorageDirectory()+"/f.txt");  
			  File file  = new File(Environment.getExternalStorageDirectory(),"f.txt");  

			   /* �ж�sd���ⲿ����״̬�Ƿ���Զ�д */  
			    if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){  
			          
			        /* ���Ķ��� *//*  */  
			     FileOutputStream fos = new FileOutputStream(file);  

			  }  
			}catch(Exception ex){  
			    Toast.makeText(MainActivity.this, "�ļ�д��ʧ��", 1000).show();  
			}  

		}
	
	
	class BTReadThread extends Thread{//��ȡ���ݵ��߳�
		private int wait = 50;// Time to wait
		public BTReadThread(int wait){
			this.wait = wait;
		}
		
		
		public void run(){
			while(enRead){
				
				//Log.d("MainActivity", "�ļ�·����-------"+Environment.getExternalStorageDirectory());
				//Log.d("MainActivity", "�ļ�·����-------"+Environment.getExternalStorageDirectory().getAbsolutePath());
					file=new File(Environment.getExternalStorageDirectory(),"101.txt");
				    //file=new File(getDefaultFilePath());
					
				    try {
				    	//Log.d("MainActivity", "bufferReader���󹹽����---------֮ǰ");
						bufferReader=new BufferedReader(new InputStreamReader(new FileInputStream(file)));
						//Log.d("MainActivity", "bufferReader���󹹽����----------֮��");
					} catch (FileNotFoundException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						Log.d("MainActivity", "bufferReader���󹹽����-----------���쳣��");
					}
					 strBuffer=new StringBuffer();
				
					try {
						while((str=bufferReader.readLine())!=null){
							Log.d("MainActivity", "bufferReader��ȡ�������-----------֮ǰ");
							strBuffer.append(str);
							
						}
						//bufferReader.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						Log.d("MainActivity", "bufferReader��ȡ�������-----------���쳣����");
					}
					revTmpStr=strBuffer.toString();
					
					if(revTmpStr.indexOf(';')!=-1){
						try{
							String ECGDataStrs[] = revTmpStr.split(";");
							for (int i = 0; i < ECGDataStrs.length -1; i++){
								try{
									ECGData = Float.parseFloat(ECGDataStrs[i].replace(';',' '));
									ECGDataList.add(ECGData);											
								}catch(Exception e){
									e.printStackTrace();
									continue;
								}

							}
							if (ECGDataStrs[ECGDataStrs.length -1].length()==6 || ECGDataStrs[ECGDataStrs.length -1].length()==7&&ECGDataStrs[ECGDataStrs.length -1].indexOf('-')==0){
								try{
									ECGData = Float.parseFloat(ECGDataStrs[ECGDataStrs.length -1].replace(';',' '));
									ECGDataList.add(ECGData);
								}catch(Exception e){
									e.printStackTrace();
								}
								revTmpStr = "";
							}
							else{
								revTmpStr = ECGDataStrs[ECGDataStrs.length -1];
							}									
							
						}
						catch(Exception e){
							e.printStackTrace();
						}		
				
			}
		}
	}
}
}

