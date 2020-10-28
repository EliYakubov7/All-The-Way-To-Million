package com.example.allthewaytomillion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static com.example.allthewaytomillion.MainActivity.MY_PREFS_NAME;

public class MenuActivity extends AppCompatActivity {

    ImageView logo;
    Button startGame,about,highScores,quit;

    //    MediaPlayer mediaPlayer;
    TextView gain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);

        ImageView animation_image_frame = findViewById(R.id.ivAnimationTitle);
        AnimationDrawable animationDrawable = (AnimationDrawable)animation_image_frame.getDrawable();
        animationDrawable.setOneShot(true);
        animationDrawable.start();

        startGame = findViewById(R.id.start_game);
//        highScores = findViewById(R.id.high_scores);
        about = findViewById(R.id.about);
        quit = findViewById(R.id.quit);

        gain = findViewById(R.id.tvScoreMenu);

//        mediaPlayer = MediaPlayer.create(MenuActivity.this, R.raw.opening);
//        mediaPlayer.start();

//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                startGame.setEnabled(true);
//                about.setEnabled(true);
////                highScores.setEnabled(true);
//                quit.setEnabled(true);
//            }
//
//        });

        RotateNow();


        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

//        highScores.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent infoHighScoresActivity = new Intent(MenuActivity.this, HighScoresActivity.class);
//                startActivity(infoHighScoresActivity);
//            }
//        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent infoGameActivity = new Intent(MenuActivity.this, InfoGameActivity.class);
                startActivity(infoGameActivity);

            }
        });

        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void RotateNow() {
        logo =  findViewById(R.id.logo);
        RotateAnimation rotate = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(7500);
        rotate.setRepeatCount(Animation.ABSOLUTE);
        rotate.setInterpolator(new LinearInterpolator());
        logo.startAnimation(rotate);
    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String money = prefs.getString("score", "0");
        gain.setText(money);    }

}
