package jp.live2d.sample;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static jp.live2d.sample.MainHaruActivity.auth;
import static jp.live2d.sample.MainHaruActivity.context;
import static jp.live2d.sample.MainHaruActivity.mRecyclerView;
import static jp.live2d.sample.MainHaruActivity.messageAdapter;
import static jp.live2d.sample.MainHaruActivity.tag;
import static jp.live2d.sample.MainHaruActivity.username;
import static jp.live2d.sample.MainHaruActivity.messages;


public class ChatRespond {

    private static String returnVal;
    /**
     * Everything that you can get response from the bot
     * @param input user's input
     * @return a message string
     */
    public static String response(String input){
        //sign out
        if (input.contains("sign out")){
            try {
                auth.signOut();
                String ret = "Bye " + username;
                username = "unknown";
                tag = "unknown";
                return ret;
            }catch (Exception e){
                return "Failed to sign out.";
            }
        }

        //get ascii emoji and copy to clipboard
        if (input.contains("kaomoji")){
            String ret = Kaomoji.randomkaomoji(input);
            ClipboardManager clipboard = (ClipboardManager)
                    MainHaruActivity.context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Kaomoji", ret);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(MainHaruActivity.context, ret + "on your clipboard", Toast.LENGTH_LONG).show();
            return ret;
        }

        //get uiuc course status information
        //an example of pulling information online
        if (input.contains("course")){
            try {
                return new MyAsyncTask().execute(input).get();
            }catch (Exception e){
                return "?";
            }
        }

        //get current weather by city name
        // an example of using api
        if (input.contains("weather")){
            try {
                return new MyAsyncTask().execute(input).get();
            }catch (Exception e){
                return "?";
            }
        }

        // set the username to firebase
        // an example of write to firebase
        if ((input.contains("i'm") || input.contains("i am")) && (!input.contains(" a ") || !input.contains(" an "))
                || input.contains("my name is")){
            try{
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference mUserReference  = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(uid).child("username");
                String username = input.substring(input.lastIndexOf(" ")+ 1);
                username = username.substring(0, 1).toUpperCase() + username.substring(1);
                mUserReference.setValue(username);
                MainHaruActivity.username = username;
                return username + "? I like this name！I'll call you " + username + ".";
            }catch (Exception e){
                return e.getMessage();
            }
        }

        //  post things into firebase, for users interaction
        if (input.startsWith("post")){
            DatabaseReference mPostReference = FirebaseDatabase.getInstance().getReference()
                    .child("posts");
            String time = DateFormat.format("MM/dd/yy HH:mm:ss", new Date().getTime()).toString();
            mPostReference.push().setValue(new Message(time, input, username));
            ArrayList<String> ret = new ArrayList<>(
                    Arrays.asList("Really?", "Cool!", "Wow~"));
            Collections.shuffle(ret);
            return ret.get(0) + " " + Kaomoji.randomkaomoji("happy");
        }

        // pull posts from firebase
        if (input.contains("tell me")){
            DatabaseReference mPostReference = FirebaseDatabase.getInstance().getReference()
                    .child("posts");

                mPostReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> postData = dataSnapshot.getChildren();
                        for (DataSnapshot data: postData){
                            Message mPost = data.getValue(Message.class);
                            mPost.setAuthor("Haru heard from " + mPost.getAuthor());
                            messages.add(mPost);
                        }
                        try{
                            Thread.sleep(1000);
                        }catch (Exception e){
                            Log.e("Thread", e.getMessage());
                        }
                        messageAdapter.notifyDataSetChanged();
                        if (messages != null && messages.size() != 0) {
                            mRecyclerView.smoothScrollToPosition(messages.size() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("cancel", databaseError.getMessage());
                    }
                });


            return "Do you have something to share with Haru?";
        }
        if (input.startsWith("choose")){
            String[] choices = input.substring(input.indexOf(" ") + 1).split(",");
            if (choices.length <= 1){
                return "I can choose one for you! Use \"choose choice1,choice2,choice3...\"";
            }
            int index = (int)(Math.random()*choices.length);
            return "Haru would like to choose " + choices[index];
        }


        //Simple sentence reply from the bot
        if (input.equals("?")){
            return Kaomoji.randomkaomoji("?");
        }
        if (input.contains("take")&& (input.contains("photo") || input.contains("picture"))){
            return "Nice photo!";
        }
        if (input.contains("play")){
            if (!input.contains(" ")){
                return "Play music for you. Use \"play exactname\".";
            }
            else return "(ﾟ3ﾟ)～♪";
        }
        if (input.contains("alarm")){
            if (!input.contains(":")){
                return "You can set alrm clock by \"alarm HH:mm\". The hour is 0-24 ~";
            }
            return "Done!";
        }
        if (input.contains("hello") || input.contains("hi")){
            return "Hi!";
        }
        if (input.contains("good morning")) return "Good morning";
        if (input.contains("good evening")) return "Good evening~";
        if (input.contains("good night") || input.contains("goodnight")) return "Good night! Have a nice dream.";
        if (input.contains("who are you")) {
            return "I'm Haru! An artificial stupidity. You can reply \"?\" to get more information about me!";
        }
        if (input.contains("nice to meet you")){
            return "Nice to meet you too.";
        }
        if (input.contains("who am i")) return "You are " + username;
        if (input.contains("haru")) return "Yes?";


        return "...";
    }

    /**
     * get the user's name from firebase
     * also decide the welcome sentence for user
     */
    public static void welcome(){
        try{
            tag = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference mUserReference  = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(tag).child("username");
            mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    username = dataSnapshot.getValue().toString();
                    String time = DateFormat.format("MM/dd/yy HH:mm:ss", new Date().getTime()).toString();
                    String welcome;
                    if (!username.equals("unknown")){
                        welcome = "Welcome back, " + username + Kaomoji.randomkaomoji("happy");
                    }else welcome = "Hello, my name is Haru. What's your name?";
                    Message mMessage = new Message(time, welcome, "Haru");
                    DatabaseReference mMsgReference  = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(tag).child("message");
                    mMsgReference.push().setValue(mMessage);
                    messages.add(mMessage);

                    messageAdapter.notifyDataSetChanged();
                    if (messages != null && messages.size() != 0) {
                        mRecyclerView.smoothScrollToPosition(messages.size() - 1);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }catch (Exception e){
            Log.e("Welcome", e.getMessage());
        }
    }


}

