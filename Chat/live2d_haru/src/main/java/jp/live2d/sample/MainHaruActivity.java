/**
 * You can modify and use this source freely
 * only for the development of application related Live2D.
 * <p>
 * (c) Live2D Inc. All rights reserved.
 */

package jp.live2d.sample;

import jp.live2d.utils.android.FileManager;
import jp.live2d.utils.android.SoundManager;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


/**
 * Main activity, launched after login
 */
public class MainHaruActivity extends Activity {
    //  Live2D management
    private LAppLive2DManager live2DMgr;
    static private Activity instance;

    // UI management
    public EditText input;
    public ImageButton talkButton;
    public ImageButton sendButton;
    public ImageButton helpButton;
    public DatabaseReference mMsgReference;
    public File output = null;

    //UI stuff that are used outside of this class
    protected static Context context;
    protected static MessageAdapter messageAdapter;
    protected static ArrayList<Message> messages = new ArrayList<>();
    protected static RecyclerView mRecyclerView;
    protected static String tag = "unknown";
    protected static String username = "unknown";
    protected static FirebaseAuth auth = FirebaseAuth.getInstance();


    //static request code, just a random int
    private final int SPEECH_REQUEST_CODE = 100;
    private static final int CAMERA_PHOTO_REQUEST_CODE = 1337;

    public MainHaruActivity() {
        instance = this;
        if (LAppDefine.DEBUG_LOG) {
            Log.d("model", "live 2D model built");
        }

        SoundManager.init(this);
        live2DMgr = new LAppLive2DManager();
    }


    static public void exit() {
        SoundManager.release();
        instance.finish();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //since the login activity is not in this module, login should be done again in this activity
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");


        LAppView view = live2DMgr.createView(this);
        FrameLayout layout = (FrameLayout) findViewById(R.id.live2DLayout);
        layout.addView(view, 0, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        FileManager.init(this.getApplicationContext());


        mMsgReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(tag).child("message");

        //initialize the UI stuff
        input = (EditText) findViewById(R.id.editTextInput);
        talkButton = (ImageButton) findViewById(R.id.talkButton);
        sendButton = (ImageButton) findViewById(R.id.sendButton);
        helpButton = (ImageButton) findViewById(R.id.helpButton);
        context = getApplicationContext();
        talkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGoogleInputDialog();
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // Set up a new adapter and assign it to recycle view
        messageAdapter = new MessageAdapter(messages);
        mRecyclerView.setAdapter(messageAdapter);


        try {
            tag = FirebaseAuth.getInstance().getCurrentUser().getUid();
            ChatRespond.welcome();

        } catch (Exception e) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        //present error message
                        Toast.makeText(getApplicationContext(),
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();

                    } else {
                        String time = DateFormat.format("MM/dd/yy HH:mm:ss", new Date().getTime()).toString();
                        ChatRespond.welcome();
                    }
                }
            });
        }


        //send button on click listener, sent message to screen and get response here
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mInput = input.getText().toString();

                //clear the edit text
                input.setText("");

                //initialize message and put to firebase
                String time = DateFormat.format("MM/dd/yy HH:mm:ss", new Date().getTime()).toString();
                Message mMessage = new Message(time, mInput, "Me");
                mMsgReference.push().setValue(mMessage);
                messages.add(mMessage);

                //display message on screen
                messageAdapter.notifyDataSetChanged();
                if (messages != null && messages.size() != 0) {
                    mRecyclerView.smoothScrollToPosition(messages.size() - 1);
                }

                // get response
                Message mResponse = new Message(time,
                        ChatRespond.response(mInput.toLowerCase()), "Haru");
                mMsgReference.push().setValue(mResponse);
                messages.add(mResponse);

                messageAdapter.notifyDataSetChanged();
                mRecyclerView.smoothScrollToPosition(messages.size() - 1);
                newIntent(mInput);
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainHaruActivity.this, HelpActivity.class));
            }
        });

    }


    /**
     * This class is for google voice input
     */
    public void showGoogleInputDialog() {
        //start google's voice input
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Device not supported",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This is for the startActivityForResult class
     * launch the camera or play a music
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SPEECH_REQUEST_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    input.setText(result.get(0));
                }
                break;
            }
            case CAMERA_PHOTO_REQUEST_CODE: {
                if (resultCode == RESULT_OK) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setDataAndType(Uri.fromFile(output), "image/jpeg");
                    startActivity(i);
                    //finish();
                }
                break;
            }

        }
    }


    @Override
    protected void onResume() {
        //live2DMgr.onResume() ;
        super.onResume();
    }


    @Override
    protected void onPause() {
        //live2DMgr.onPause() ;
        super.onPause();
    }


    /**
     * Since the startActivityForResult cannot be open in any other class
     * all intent that are needed to be add is in this class
     *
     * @param mInput the input of the user
     */
    public void newIntent(String mInput) {
        try {
            String lowerInput = mInput.toLowerCase();

            /*
             * This block of code is to open the camera and take a photo
             * and then save the photo to the default gallery
             */
            if (lowerInput.contains("take") &&
                    (lowerInput.contains("photo") || lowerInput.contains("picture"))) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                String time = DateFormat.format("MM_dd_yy_HH_mm_ss", new Date().getTime()).toString();
                output = new File(dir, "haru_" + time + ".jpeg");
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
                startActivityForResult(i, CAMERA_PHOTO_REQUEST_CODE);
                return;
            }

            /*
             * this block is to open the help menu
             */
            if (mInput.contains("?")) {
                startActivity(new Intent(MainHaruActivity.this, HelpActivity.class));
                return;
            }

            /*
             * This block is used to play a music by the exact name of the file
             * music should be placed into the default music directory
             */
            if (lowerInput.startsWith("play")) {
                final String filename = mInput.substring(mInput.indexOf("play") + 5);

                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                File file = new File(dir, filename + ".mp3");
                intent.setDataAndType(Uri.fromFile(file), "audio/mp3");
                startActivity(intent);
                return;
            }

            /*
             * This block is used to set an alarm clock
             * hour and minute is needed
             * the alarm is set to the next day
             */
            if (lowerInput.contains("alarm")) {
                Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
                if (lowerInput.contains(":")) {
                    int hour = Integer.parseInt(lowerInput.substring
                            (lowerInput.indexOf(":") - 2, lowerInput.indexOf(":")));
                    int minute = Integer.parseInt(lowerInput.substring
                            (lowerInput.indexOf(":") + 1, lowerInput.indexOf(":") + 3));
                    if (hour >= 0 && hour < 24) {
                        i.putExtra(AlarmClock.EXTRA_HOUR, hour);
                    }
                    if (minute >= 0 && minute < 60) {
                        i.putExtra(AlarmClock.EXTRA_MINUTES, minute);
                    }
                }
                startActivity(i);
                return;
            }

            if (mInput.contains("sign in")) {
                String[] signIn = mInput.split(" ");
                int size = signIn.length;
                auth.signInWithEmailAndPassword(signIn[size - 2], signIn[size - 1])
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    //present error message
                                    Toast.makeText(getApplicationContext(),
                                            task.getException().getMessage(), Toast.LENGTH_LONG).show();

                                } else {
                                    String time = DateFormat.format("MM/dd/yy HH:mm:ss", new Date().getTime()).toString();
                                    messages.add(new Message(time, "Login success", "Haru"));
                                    messageAdapter.notifyDataSetChanged();
                                    if (messages != null && messages.size() != 0) {
                                        mRecyclerView.smoothScrollToPosition(messages.size() - 1);
                                    }
                                    ChatRespond.welcome();
                                }
                            }
                        });

                return;
            }

            //method to sign up in the activity
            if (mInput.contains("sign up")) {
                String[] signIn = mInput.split(" ");
                int size = signIn.length;
                auth.createUserWithEmailAndPassword(signIn[size - 2], signIn[size - 1])
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    //present error message
                                    Toast.makeText(getApplicationContext(),
                                            task.getException().getMessage(), Toast.LENGTH_LONG).show();

                                } else {
                                    String time = DateFormat.format("MM/dd/yy HH:mm:ss", new Date().getTime()).toString();
                                    messages.add(new Message(time, "Register success", "Haru"));
                                    messageAdapter.notifyDataSetChanged();
                                    if (messages != null && messages.size() != 0) {
                                        mRecyclerView.smoothScrollToPosition(messages.size() - 1);
                                    }
                                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    DatabaseReference mUserReference  = FirebaseDatabase.getInstance().getReference()
                                            .child("users").child(uid).child("username");
                                    mUserReference.setValue("unknown");
                                    ChatRespond.welcome();
                                    //startActivity(new Intent(MainHaruActivity.this, HelpActivity.class));
                                }
                            }
                        });
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


}
