package jp.live2d.sample;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import java.util.Date;

/**
 * method about the authentication stuff
 */
public class AccountManager extends MainHaruActivity{


    public void account() {
        while(tag.equals("unknown")) {
            String time = DateFormat.format("MM/dd/yy HH:mm:ss", new Date().getTime()).toString();
            String ret = "Please sign in at first! \"sign in username password\" or \"sign up username password\"";
            Message mMessage = new Message(time, ret, "Haru");
            messages.add(mMessage);
            messageAdapter.notifyDataSetChanged();

            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String mInput = input.getText().toString();
                    String mInput2 = mInput.substring(0, mInput.lastIndexOf(" "));

                    //clear the edit text
                    input.setText("");

                    //initialize message and put to firebase
                    String time = DateFormat.format("MM/dd/yy HH:mm:ss", new Date().getTime()).toString();
                    Message mMessage = new Message(time, mInput2 + " *******", "Me");
                    mMsgReference.push().setValue(mMessage);
                    messages.add(mMessage);

                    //display message on screen
                    messageAdapter.notifyDataSetChanged();
                    mRecyclerView.smoothScrollToPosition(messages.size() - 1);

                    signin(mInput);

                }
            });



        }

    }

    public void signin(String input){
        //method to login in the activity

    }
}
