package jp.live2d.sample;

import android.os.AsyncTask;

import java.net.URL;

public class MyAsyncTask extends AsyncTask<String, Void, String> {
        public MyAsyncTask(){}
        @Override
        protected String doInBackground(String... params) {
            try {
                if(params[0].contains("course")){
                    return CourseExplorer.status(params[0]);
                }
                if(params[0].contains("weather")){
                    return Weather.weather(params[0]);
                }
                return "What's that?";
            } catch (Exception e) {
                return e.getMessage();
            }
        }

}
