package com.example.allthewaytomillion;


import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.view.circulartimerview.CircularTimerListener;
import com.view.circulartimerview.CircularTimerView;
import com.view.circulartimerview.TimeFormatEnum;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;


public class MainActivity extends AppCompatActivity {

    //--------------------Save questions level for not repeat----------------------------------------------------------
    ArrayList<Integer> easyQuestions = new ArrayList<Integer>();
    ArrayList<Integer> normalQuestions = new ArrayList<Integer>();
    ArrayList<Integer> hardQuestions = new ArrayList<Integer>();
    //-------------------------------------Firebase Variables----------------------------------------------------------
    private TextView mQuestionView;
    public static String mAnswer;
    public static String choice1, choice2, choice3, choice4;
    //-------------------------------------Count Questions----------------------------------------------------------
    @TargetApi(Build.VERSION_CODES.O)
    private int mQuestionNumber = 0;    //  for repeat gameIsStart music
    private int mNumberOfQuestion = 0;  // for change levFel questions
    //-------------------------------------Timer-------------------------------------------------------------------
    private CircularTimerView progressBarTime;
    //-------------------------------------Count Questions----------------------------------------------------------
    @SuppressLint("StaticFieldLeak")
    private static Button mButtonChoice1, mButtonChoice2, mButtonChoice3, mButtonChoice4; // 4 answers
    Button[] buttons = new Button[15]; // All button side with price
    private Button leave; // Leave Button
    //-------------------------------------Pop Up win/lose game------------------------------------------------------
    Dialog popUpDialog;
    Button mainMenu; // go to menu
    Button playAgain; // start new game
    //-------------------------------------Your help in the game - 3 save lifeCycle-----------------------------------
    ImageButton change_question, fifty_fifty, audience;
    public static String answerOfAudience; // for fragment
    //-------------------------------------music game-----------------------------------------------------------------
//    public MediaPlayer mediaPlayer;
//-------------------------------------general sharedPreferences--------------------------------------------------
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    //-------------------------------------Save answers for 50 : 50 Help-----------------------------------------------
    ArrayList<Button> positions = new ArrayList<>(); // save all answers
    ArrayList<Button> save = new ArrayList<>(); // save only wrong answers and do shuffle
    //-------------------------------------Keep on background button when question is update----------------------------
    public int counterButton = 0;
    //-------------------------------------Display gain of money and message on Pop Up-----------------------------------------------
    TextView money;
    TextView message;
    //-------------------------------------Save highScore and display on main menu screen---------------------------------
    int previousValue = 0;
    int currentValue = 0;
    //-------------------------------------Finish Variables--------------------------------------------------------------
    int prevEasyNum;
    int prevNormalNum;
    int prevHardNum;
    int rand;
    int questionCount;
    Iterator itr;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Firebase.setAndroidContext(this);

        mButtonChoice1 = findViewById(R.id.choice1);
        mButtonChoice2 = findViewById(R.id.choice2);
        mButtonChoice3 = findViewById(R.id.choice3);
        mButtonChoice4 = findViewById(R.id.choice4);
        mQuestionView = findViewById(R.id.question);

        buttons[0] = findViewById(R.id.question1);
        buttons[1] = findViewById(R.id.question2);
        buttons[2] = findViewById(R.id.question3);
        buttons[3] = findViewById(R.id.question4);
        buttons[4] = findViewById(R.id.question5);
        buttons[5] = findViewById(R.id.question6);
        buttons[6] = findViewById(R.id.question7);
        buttons[7] = findViewById(R.id.question8);
        buttons[8] = findViewById(R.id.question9);
        buttons[9] = findViewById(R.id.question10);
        buttons[10] = findViewById(R.id.question11);
        buttons[11] = findViewById(R.id.question12);
        buttons[12] = findViewById(R.id.question13);
        buttons[13] = findViewById(R.id.question14);
        buttons[14] = findViewById(R.id.question15);
        leave = findViewById(R.id.leave);
        buttons[0].setBackgroundDrawable(getResources().getDrawable(R.drawable.pass));

        change_question = findViewById(R.id.change_question);
        fifty_fifty = findViewById(R.id.fifty_fifty);
        audience = findViewById(R.id.audience);


//        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.gameisstart);
//        mediaPlayer.start();

        if (mNumberOfQuestion >= 0 && mNumberOfQuestion <= 4) {
            displayEasyQuestion();
        } else if (mNumberOfQuestion >= 5 && mNumberOfQuestion <= 9) {
            displayNormalQuestion();
        } else {
            displayHardQuestion();
        }

        popUpDialog = new Dialog(this);
        popUpDialog.setContentView(R.layout.pop_up_game);
        Objects.requireNonNull(popUpDialog.getWindow()).getAttributes().windowAnimations = R.style.ScorePopUpAnimation;
        popUpDialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);

        money = popUpDialog.findViewById(R.id.tvScore);
        message = popUpDialog.findViewById(R.id.tvCongratulations);

        popUpDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                String check = prefs.getString("score", "0");
                String oldScore = check.replace(",","");

                String temp = money.getText().toString();
                String newScore = temp.replace(",","");

                previousValue = Integer.parseInt(oldScore);
                currentValue = Integer.parseInt(newScore);
                if (previousValue <= currentValue) {
                    String scoreWithComma = NumberFormat.getIntegerInstance().format(currentValue);
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("score", scoreWithComma);
                    editor.apply();
                }
            }
        });

        mainMenu = popUpDialog.findViewById(R.id.main_menu);

        mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        playAgain = popUpDialog.findViewById(R.id.play_again);

        playAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

            }
        });

        progressBarTime = findViewById(R.id.timer);
        progressBarTime.setProgress(0);

        progressBarTime.setCircularTimerListener(new CircularTimerListener() {
            @Override
            public String updateDataOnTick(long remainingTimeInMs) {
                return String.valueOf((int) Math.ceil((remainingTimeInMs / 1000.f)));
            }

            @Override
            public void onTimerFinished() {

                timerIsOut();
                showPopup();

            }
        }, 30, TimeFormatEnum.SECONDS, 30);

        progressBarTime.startTimer();



        fifty_fifty.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {

                positions.add(mButtonChoice1);
                positions.add(mButtonChoice2);
                positions.add(mButtonChoice3);
                positions.add(mButtonChoice4);


                for (int i = 0; i < positions.size(); i++) {
                    if (!positions.get(i).getText().equals(mAnswer)) {
                        save.add(positions.get(i));
                    }
                }

                Collections.shuffle(save);

                save.get(0).setText("");
                save.get(0).setEnabled(false);

                save.get(1).setText("");
                save.get(1).setEnabled(false);


                fifty_fifty.setEnabled(false);
                fifty_fifty.setImageDrawable(getResources().getDrawable(R.drawable.fifty_fifty_used));
//              fifty_fifty.setVisibility(View.GONE);
                fifty_fifty.setOnClickListener(null);

            }

        });

        change_question.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {

                mNumberOfQuestion--;

                if (mNumberOfQuestion >= 0 && mNumberOfQuestion <= 4) {
                    flipView();
                    displayEasyQuestion();
                    resetTimer();
                    mButtonChoice1.setBackground(getDrawable(R.drawable.background_answer));
                } else if (mNumberOfQuestion >= 5 && mNumberOfQuestion <= 9) {
                    flipView();
                    displayNormalQuestion();
                    resetTimer();
                    mButtonChoice1.setBackground(getDrawable(R.drawable.background_answer));
                } else {
                    flipView();
                    displayHardQuestion();
                    resetTimer();
                    mButtonChoice1.setBackground(getDrawable(R.drawable.background_answer));
                }

                change_question.setEnabled(false);
                change_question.setImageDrawable(getResources().getDrawable(R.drawable.change_question_used));
//              change_question.setVisibility(View.GONE);
                change_question.setOnClickListener(null);
            }
        });

        audience.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {

                Audience dialogFragment = new Audience();
                FragmentManager fm = getFragmentManager();
                dialogFragment.show(fm, "audience");


                audience.setEnabled(false);
                audience.setImageDrawable(getResources().getDrawable(R.drawable.audience_used));
//              audience.setVisibility(View.GONE);
                audience.setOnClickListener(null);
            }
        });

        leave.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogCustom));
                builder.setMessage("האם אתה בטוח שברצונך לפרוש מהמשחק ?");
                builder.setCancelable(true);

                builder.setPositiveButton(
                        "כן",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                progressBarTime.stopTimer();
                                leaveGame();
                                showPopup();

                            }
                        }).setPositiveButtonIcon(getDrawable(R.drawable.open_door));

                builder.setNegativeButton(
                        "לא",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).setNegativeButtonIcon(getDrawable(R.drawable.close_door));

                AlertDialog alert = builder.create();
                alert.show();
            }
        });


        mButtonChoice1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                progressBarTime.stopTimer();

//                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.check_answer);
//                mediaPlayer.start();
                mButtonChoice1.setBackground(getDrawable(R.drawable.check));
                mButtonChoice1.setTextColor(Color.WHITE);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mButtonChoice1.getText().equals(mAnswer)) {

//                            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.correct_answer);
//                            mediaPlayer.start();
                            mButtonChoice1.setBackground(getDrawable(R.drawable.correct));
                            mButtonChoice1.setTextColor(Color.WHITE);

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void run() {

                                    mButtonChoice1.setBackground(getDrawable(R.drawable.background_answer));

                                    mQuestionNumber++;

                                    if (mQuestionNumber == 5 || mQuestionNumber == 10) {
//                                        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.gameisstart);
//                                        mediaPlayer.start();
                                    }

                                    resetTimer();

                                    winGame();

                                    if (mNumberOfQuestion >= 0 && mNumberOfQuestion <= 4) {
                                        displayEasyQuestion();
                                        mButtonChoice1.setBackground(getDrawable(R.drawable.background_answer));
                                    } else if (mNumberOfQuestion >= 5 && mNumberOfQuestion <= 9) {
                                        displayNormalQuestion();
                                        mButtonChoice1.setBackground(getDrawable(R.drawable.background_answer));
                                    } else {
                                        displayHardQuestion();
                                        mButtonChoice1.setBackground(getDrawable(R.drawable.background_answer));
                                    }


                                    for (int i = 0; i < save.size(); i++) {
                                        if (!save.get(i).getText().equals(mAnswer)) {
                                            save.get(i).setEnabled(true);
                                        }
                                    }

                                    if (counterButton == 4 || counterButton == 9 || counterButton == 14) {
                                        buttons[counterButton].setBackgroundDrawable(getResources().getDrawable(R.drawable.yours_money_after_correct_answer));
                                    } else {
                                        buttons[counterButton].setBackgroundDrawable(getResources().getDrawable(R.drawable.you_lost_all_after_wrong_answer));
                                    }

                                    if (counterButton < 14) {
                                        buttons[counterButton + 1].setBackgroundDrawable(getResources().getDrawable(R.drawable.pass));
                                    }
                                    counterButton++;

                                }
                            }, 1500);
                        } else {
//                            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.wrong_answer);
//                            mediaPlayer.start();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    if (mButtonChoice2.getText().equals(mAnswer)) {
                                        mButtonChoice1.setBackground(getDrawable(R.drawable.wrong));
                                        mButtonChoice2.setBackground(getDrawable(R.drawable.correct));
                                    } else if (mButtonChoice3.getText().equals(mAnswer)) {
                                        mButtonChoice1.setBackground(getDrawable(R.drawable.wrong));
                                        mButtonChoice3.setBackground(getDrawable(R.drawable.correct));
                                    } else if (mButtonChoice4.getText().equals(mAnswer)) {
                                        mButtonChoice1.setBackground(getDrawable(R.drawable.wrong));
                                        mButtonChoice4.setBackground(getDrawable(R.drawable.correct));
                                    }
                                    wrongAnswer();
                                    showPopup();

                                }
                            }, 750);
                        }
                    }
                }, 750);

            }
        });


        mButtonChoice2.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                progressBarTime.stopTimer();

//                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.check_answer);
//                mediaPlayer.start();
                mButtonChoice2.setBackground(getDrawable(R.drawable.check));
                mButtonChoice2.setTextColor(Color.WHITE);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mButtonChoice2.getText().equals(mAnswer)) {

//                            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.correct_answer);
//                            mediaPlayer.start();
                            mButtonChoice2.setBackground(getDrawable(R.drawable.correct));
                            mButtonChoice2.setTextColor(Color.WHITE);

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void run() {

                                    mButtonChoice2.setBackground(getDrawable(R.drawable.background_answer));

                                    mQuestionNumber++;

                                    if (mQuestionNumber == 5 || mQuestionNumber == 10) {
//                                        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.gameisstart);
//                                        mediaPlayer.start();
                                    }

                                    resetTimer();

                                    winGame();

                                    if (mNumberOfQuestion >= 0 && mNumberOfQuestion <= 4) {
                                        displayEasyQuestion();
                                        mButtonChoice2.setBackground(getDrawable(R.drawable.background_answer));
                                    } else if (mNumberOfQuestion >= 5 && mNumberOfQuestion <= 9) {
                                        displayNormalQuestion();
                                        mButtonChoice2.setBackground(getDrawable(R.drawable.background_answer));
                                    } else {
                                        displayHardQuestion();
                                        mButtonChoice2.setBackground(getDrawable(R.drawable.background_answer));
                                    }


                                    for (int i = 0; i < save.size(); i++) {
                                        if (!save.get(i).getText().equals(mAnswer)) {
                                            save.get(i).setEnabled(true);
                                        }
                                    }

                                    if (counterButton == 4 || counterButton == 9 || counterButton == 14) {
                                        buttons[counterButton].setBackgroundDrawable(getResources().getDrawable(R.drawable.yours_money_after_correct_answer));
                                    } else {
                                        buttons[counterButton].setBackgroundDrawable(getResources().getDrawable(R.drawable.you_lost_all_after_wrong_answer));
                                    }

                                    if (counterButton < 14) {
                                        buttons[counterButton + 1].setBackgroundDrawable(getResources().getDrawable(R.drawable.pass));
                                    }
                                    counterButton++;

                                }
                            }, 1500);
                        } else {

//                            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.wrong_answer);
//                            mediaPlayer.start();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    if (mButtonChoice1.getText().equals(mAnswer)) {
                                        mButtonChoice2.setBackground(getDrawable(R.drawable.wrong));
                                        mButtonChoice1.setBackground(getDrawable(R.drawable.correct));
                                    } else if (mButtonChoice3.getText().equals(mAnswer)) {
                                        mButtonChoice2.setBackground(getDrawable(R.drawable.wrong));
                                        mButtonChoice3.setBackground(getDrawable(R.drawable.correct));
                                    } else if (mButtonChoice4.getText().equals(mAnswer)) {
                                        mButtonChoice2.setBackground(getDrawable(R.drawable.wrong));
                                        mButtonChoice4.setBackground(getDrawable(R.drawable.correct));
                                    }
                                    wrongAnswer();
                                    showPopup();

                                }
                            }, 750);
                        }
                    }
                }, 750);

            }
        });


        mButtonChoice3.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                progressBarTime.stopTimer();

//                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.check_answer);
//                mediaPlayer.start();
                mButtonChoice3.setBackground(getDrawable(R.drawable.check));
                mButtonChoice3.setTextColor(Color.WHITE);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mButtonChoice3.getText().equals(mAnswer)) {

//                            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.correct_answer);
//                            mediaPlayer.start();
                            mButtonChoice3.setBackground(getDrawable(R.drawable.correct));
                            mButtonChoice3.setTextColor(Color.WHITE);

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void run() {

                                    mButtonChoice3.setBackground(getDrawable(R.drawable.background_answer));

                                    mQuestionNumber++;

                                    if (mQuestionNumber == 5 || mQuestionNumber == 10) {
//                                        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.gameisstart);
//                                        mediaPlayer.start();
                                    }

                                    resetTimer();

                                    winGame();

                                    if (mNumberOfQuestion >= 0 && mNumberOfQuestion <= 4) {
                                        displayEasyQuestion();
                                        mButtonChoice3.setBackground(getDrawable(R.drawable.background_answer));
                                    } else if (mNumberOfQuestion >= 5 && mNumberOfQuestion <= 9) {
                                        displayNormalQuestion();
                                        mButtonChoice3.setBackground(getDrawable(R.drawable.background_answer));
                                    } else {
                                        displayHardQuestion();
                                        mButtonChoice3.setBackground(getDrawable(R.drawable.background_answer));
                                    }


                                    for (int i = 0; i < save.size(); i++) {
                                        if (!save.get(i).getText().equals(mAnswer)) {
                                            save.get(i).setEnabled(true);
                                        }
                                    }

                                    if (counterButton == 4 || counterButton == 9 || counterButton == 14) {
                                        buttons[counterButton].setBackgroundDrawable(getResources().getDrawable(R.drawable.yours_money_after_correct_answer));
                                    } else {
                                        buttons[counterButton].setBackgroundDrawable(getResources().getDrawable(R.drawable.you_lost_all_after_wrong_answer));
                                    }

                                    if (counterButton < 14) {
                                        buttons[counterButton + 1].setBackgroundDrawable(getResources().getDrawable(R.drawable.pass));
                                    }
                                    counterButton++;

                                }
                            }, 1500);
                        } else {

//                            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.wrong_answer);
//                            mediaPlayer.start();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    if (mButtonChoice1.getText().equals(mAnswer)) {
                                        mButtonChoice3.setBackground(getDrawable(R.drawable.wrong));
                                        mButtonChoice1.setBackground(getDrawable(R.drawable.correct));
                                    } else if (mButtonChoice2.getText().equals(mAnswer)) {
                                        mButtonChoice3.setBackground(getDrawable(R.drawable.wrong));
                                        mButtonChoice2.setBackground(getDrawable(R.drawable.correct));
                                    } else if (mButtonChoice4.getText().equals(mAnswer)) {
                                        mButtonChoice3.setBackground(getDrawable(R.drawable.wrong));
                                        mButtonChoice4.setBackground(getDrawable(R.drawable.correct));
                                    }
                                    wrongAnswer();
                                    showPopup();

                                }
                            }, 750);
                        }
                    }
                }, 750);

            }
        });


        mButtonChoice4.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                progressBarTime.stopTimer();

//                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.check_answer);
//                mediaPlayer.start();
                mButtonChoice4.setBackground(getDrawable(R.drawable.check));
                mButtonChoice4.setTextColor(Color.WHITE);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mButtonChoice4.getText().equals(mAnswer)) {

//                            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.correct_answer);
//                            mediaPlayer.start();
                            mButtonChoice4.setBackground(getDrawable(R.drawable.correct));
                            mButtonChoice4.setTextColor(Color.WHITE);

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void run() {

                                    mButtonChoice4.setBackground(getDrawable(R.drawable.background_answer));

                                    mQuestionNumber++;

                                    if (mQuestionNumber == 5 || mQuestionNumber == 10) {
//                                        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.gameisstart);
//                                        mediaPlayer.start();
                                    }

                                    resetTimer();

                                    winGame();

                                    if (mNumberOfQuestion >= 0 && mNumberOfQuestion <= 4) {
                                        displayEasyQuestion();
                                        mButtonChoice4.setBackground(getDrawable(R.drawable.background_answer));
                                    } else if (mNumberOfQuestion >= 5 && mNumberOfQuestion <= 9) {
                                        displayNormalQuestion();
                                        mButtonChoice4.setBackground(getDrawable(R.drawable.background_answer));
                                    } else {
                                        displayHardQuestion();
                                        mButtonChoice4.setBackground(getDrawable(R.drawable.background_answer));
                                    }


                                    for (int i = 0; i < save.size(); i++) {
                                        if (!save.get(i).getText().equals(mAnswer)) {
                                            save.get(i).setEnabled(true);
                                        }
                                    }

                                    if (counterButton == 4 || counterButton == 9 || counterButton == 14) {
                                        buttons[counterButton].setBackgroundDrawable(getResources().getDrawable(R.drawable.yours_money_after_correct_answer));
                                    } else {
                                        buttons[counterButton].setBackgroundDrawable(getResources().getDrawable(R.drawable.you_lost_all_after_wrong_answer));
                                    }

                                    if (counterButton < 14) {
                                        buttons[counterButton + 1].setBackgroundDrawable(getResources().getDrawable(R.drawable.pass));
                                    }
                                    counterButton++;

                                }
                            }, 1500);
                        } else {

//                            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.wrong_answer);
//                            mediaPlayer.start();

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
//                                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.wrong_answer);
//                                    mediaPlayer.start();
                                    if (mButtonChoice1.getText().equals(mAnswer)) {
                                        mButtonChoice4.setBackground(getDrawable(R.drawable.wrong));
                                        mButtonChoice1.setBackground(getDrawable(R.drawable.correct));
                                    } else if (mButtonChoice2.getText().equals(mAnswer)) {
                                        mButtonChoice4.setBackground(getDrawable(R.drawable.wrong));
                                        mButtonChoice2.setBackground(getDrawable(R.drawable.correct));
                                    } else if (mButtonChoice3.getText().equals(mAnswer)) {
                                        mButtonChoice4.setBackground(getDrawable(R.drawable.wrong));
                                        mButtonChoice3.setBackground(getDrawable(R.drawable.correct));
                                    }
                                    wrongAnswer();
                                    showPopup();
                                }
                            }, 750);
                        }
                    }
                }, 750);

            }
        });
    }

    //------------------------------------------------------------------------ Audience Fragment----------------------------------------------------------------------------
    public static class Audience extends DialogFragment {

        Button thanksForAudienceVote;
        ImageView a, b, c, d;
        ImageView accuracy;
        int total = 10;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.audience_vote, container, false);

            answerOfAudience = mAnswer;

            data(rootView);

            ArrayList<ImageView> saveImages = new ArrayList<ImageView>();

            ArrayList<ImageView> images = new ArrayList<ImageView>();
            images.add(a);
            images.add(b);
            images.add(c);
            images.add(d);

            ArrayList<String> strings = new ArrayList<String>();
            strings.add(choice1);
            strings.add(choice2);
            strings.add(choice3);
            strings.add(choice4);

            for (int i = 0; i < strings.size(); i++) {
                if (strings.get(i).equals(answerOfAudience)) {
                    accuracy = images.get(i);
                    Random r = new Random();
                    int low = 4;
                    int high = 8;
                    int Result = r.nextInt(high - low) + low;
                    total = total - Result;
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) accuracy.getLayoutParams();
                    params.weight = (float) Result;
                    accuracy.setLayoutParams(params);
                } else {
                    saveImages.add(images.get(i));
                }
            }

            int[] divide = percentages(total, 3);
            for (int i = 0; i < saveImages.size(); i++) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) saveImages.get(i).getLayoutParams();
                params.weight = (float) divide[i];
                saveImages.get(i).setLayoutParams(params);
            }

            thanksForAudienceVote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getDialog().dismiss();
                }
            });

            return rootView;
        }

        void data(View view) {
            thanksForAudienceVote = view.findViewById(R.id.dismissss);
            a = view.findViewById(R.id.ia);
            b = view.findViewById(R.id.ib);
            c = view.findViewById(R.id.ic);
            d = view.findViewById(R.id.id);
        }
    }

//------------------------------------------------------ Method for divide result on 3 wrong answers---------------------------------------------------------------------

    public static int[] percentages(int number, int number_of_parts) {
        Random r = new Random();
        HashSet<Integer> uniqueInts = new HashSet<Integer>();
        uniqueInts.add(0);
        uniqueInts.add(number);
        int array_size = number_of_parts + 1;
        while (uniqueInts.size() < array_size) {
            uniqueInts.add(1 + r.nextInt(number - 1));
        }
        Integer[] dividers = uniqueInts.toArray(new Integer[array_size]);
        Arrays.sort(dividers);
        int[] results = new int[number_of_parts];
        for (int i = 1, j = 0; i < dividers.length; ++i, ++j) {
            results[j] = dividers[i] - dividers[j];
        }
        return results;
    }

//------------------------------------------------------ Method for popup when game is finished---------------------------------------------------------------------

    private void showPopup() {
        Objects.requireNonNull(popUpDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popUpDialog.setCanceledOnTouchOutside(false);
        if (!MainActivity.this.isFinishing()) {
            popUpDialog.show();
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putInt("timesPlayed",timesPlayed).apply();
        }
        popUpDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
    }

    //-------------------------------------------------------Method for display easy questions - first 5 questions------------------------------------------------------------
    public void displayEasyQuestion() {
        final Query questionToDisplay = FirebaseDatabase.getInstance().getReference().child("easy");

        questionToDisplay.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                prevEasyNum = prefs.getInt("prevEasy", 0);

                easyQuestions.add(prevEasyNum);

                int questionCount = (int) dataSnapshot.getChildrenCount();
                Random random = new Random();
                rand = random.nextInt(questionCount);


                for (int i = 0; i < easyQuestions.size(); i++) {
                    if (rand == easyQuestions.get(i)) {
                        rand = random.nextInt(questionCount);
                    }
                }

                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt("prevEasy", rand);
                editor.apply();


                itr = dataSnapshot.getChildren().iterator();

                for (int i = 0; i < rand; i++) {
                    itr.next();
                }


                if (mNumberOfQuestion < 5) {
                    DataSnapshot childSnapshot = (DataSnapshot) itr.next();
                    String question = childSnapshot.child("question").getValue(String.class);
                    mQuestionView.setText(question);
                    choice1 = childSnapshot.child("choice1").getValue(String.class);
                    mButtonChoice1.setText(choice1);
                    choice2 = childSnapshot.child("choice2").getValue(String.class);
                    mButtonChoice2.setText(choice2);
                    choice3 = childSnapshot.child("choice3").getValue(String.class);
                    mButtonChoice3.setText(choice3);
                    choice4 = childSnapshot.child("choice4").getValue(String.class);
                    mButtonChoice4.setText(choice4);
                    mAnswer = childSnapshot.child("answer").getValue(String.class);
                    mNumberOfQuestion += 1;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //-------------------------------------------------------Method for display normal questions - middle 5 questions-----------------------------------------------------------
    public void displayNormalQuestion() {

        final Query questionToDisplay = FirebaseDatabase.getInstance().getReference().child("normal");

        questionToDisplay.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                prevNormalNum = prefs.getInt("prevNormal", 0);

                normalQuestions.add(prevNormalNum);

                questionCount = (int) dataSnapshot.getChildrenCount();
                Random random = new Random();
                rand = random.nextInt(questionCount);


                for (int i = 0; i < normalQuestions.size(); i++) {
                    if (rand == normalQuestions.get(i)) {
                        rand = random.nextInt(questionCount);
                    }
                }

                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt("prevNormal", rand);
                editor.apply();

                itr = dataSnapshot.getChildren().iterator();

                for (int i = 0; i < rand; i++) {
                    itr.next();
                }

                if (mNumberOfQuestion >= 5 && mNumberOfQuestion < 10) {
                    DataSnapshot childSnapshot = (DataSnapshot) itr.next();
                    String question = childSnapshot.child("question").getValue(String.class);
                    mQuestionView.setText(question);
                    choice1 = childSnapshot.child("choice1").getValue(String.class);
                    mButtonChoice1.setText(choice1);
                    choice2 = childSnapshot.child("choice2").getValue(String.class);
                    mButtonChoice2.setText(choice2);
                    choice3 = childSnapshot.child("choice3").getValue(String.class);
                    mButtonChoice3.setText(choice3);
                    choice4 = childSnapshot.child("choice4").getValue(String.class);
                    mButtonChoice4.setText(choice4);
                    mAnswer = childSnapshot.child("answer").getValue(String.class);
                    mNumberOfQuestion += 1;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //-------------------------------------------------------Method for display hard questions - last 5 questions-------------------------------------------------------------
    public void displayHardQuestion() {

        final Query questionToDisplay = FirebaseDatabase.getInstance().getReference().child("hard");

        questionToDisplay.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                prevHardNum = prefs.getInt("prevHard", 0);

                hardQuestions.add(prevHardNum);

                questionCount = (int) dataSnapshot.getChildrenCount();
                Random random = new Random();
                rand = random.nextInt(questionCount);


                for (int i = 0; i < hardQuestions.size(); i++) {
                    if (rand == hardQuestions.get(i)) {
                        rand = random.nextInt(questionCount);
                    }
                }

                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt("prevHard", rand);
                editor.apply();

                itr = dataSnapshot.getChildren().iterator();

                for (int i = 0; i < rand; i++) {
                    itr.next();
                }

                if (mNumberOfQuestion >= 10 && mNumberOfQuestion < 15) {
                    DataSnapshot childSnapshot = (DataSnapshot) itr.next();
                    String question = childSnapshot.child("question").getValue(String.class);
                    mQuestionView.setText(question);
                    choice1 = childSnapshot.child("choice1").getValue(String.class);
                    mButtonChoice1.setText(choice1);
                    choice2 = childSnapshot.child("choice2").getValue(String.class);
                    mButtonChoice2.setText(choice2);
                    choice3 = childSnapshot.child("choice3").getValue(String.class);
                    mButtonChoice3.setText(choice3);
                    choice4 = childSnapshot.child("choice4").getValue(String.class);
                    mButtonChoice4.setText(choice4);
                    mAnswer = childSnapshot.child("answer").getValue(String.class);
                    mNumberOfQuestion += 1;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //-------------------------------------------------------How many you will earn if you will leave the game----------------------------------------------------------------
    @SuppressLint("SetTextI18n")
    public void leaveGame() {

        if (mNumberOfQuestion == 1) {
            message.setText("בחרת לפרוש מהמשחק");
            money.setText("0");
        } else if (mNumberOfQuestion == 2) {
            message.setText("בחרת לפרוש מהמשחק");
            money.setText("100");
        } else if (mNumberOfQuestion == 3) {
            message.setText("בחרת לפרוש מהמשחק");
            money.setText("200");
        } else if (mNumberOfQuestion == 4) {
            message.setText("בחרת לפרוש מהמשחק");
            money.setText("300");
        } else if (mNumberOfQuestion == 5) {
            message.setText("בחרת לפרוש מהמשחק");
            money.setText("500");
        } else if (mNumberOfQuestion == 6) {
            message.setText("בחרת לפרוש מהמשחק");
            money.setText("1,000");
        } else if (mNumberOfQuestion == 7) {
            message.setText("בחרת לפרוש מהמשחק");
            money.setText("2,000");
        } else if (mNumberOfQuestion == 8) {
            message.setText("בחרת לפרוש מהמשחק");
            money.setText("4,000");
        } else if (mNumberOfQuestion == 9) {
            message.setText("בחרת לפרוש מהמשחק");
            money.setText("8,000");
        } else if (mNumberOfQuestion == 10) {
            message.setText("בחרת לפרוש מהמשחק");
            money.setText("16,000");
        } else if (mNumberOfQuestion == 11) {
            message.setText("בחרת לפרוש מהמשחק");
            money.setText("32,000");
        } else if (mNumberOfQuestion == 12) {
            message.setText("בחרת לפרוש מהמשחק");
            money.setText("64,000");
        } else if (mNumberOfQuestion == 13) {
            message.setText("בחרת לפרוש מהמשחק");
            money.setText("125,000");
        } else if (mNumberOfQuestion == 14) {
            message.setText("בחרת לפרוש מהמשחק");
            money.setText("250,000");
        } else if (mNumberOfQuestion == 15) {
            message.setText("בחרת לפרוש מהמשחק");
            money.setText("500,000");
        }
    }

    //-------------------------------------------------------How many you will earn if your time is go out-----------------------------------------------------------------
    @SuppressLint("SetTextI18n")
    public void timerIsOut() {

        if (mNumberOfQuestion >= 1 && mNumberOfQuestion < 6) {
            message.setText("אופס נגמר הזמן");
            money.setText("0");
        } else if (mNumberOfQuestion > 5 && mNumberOfQuestion <= 10) {
            message.setText("שים לב לזמן בפעם הבאה");
            money.setText("1,000");
        } else if (mNumberOfQuestion > 10 && mNumberOfQuestion <= 15) {
            message.setText("הזמן גמר אותך");
            money.setText("32,000");
        }
    }

    //-------------------------------------------------------How many you will earn if your answer will be wrong--------------------------------------------------------------
    @SuppressLint("SetTextI18n")
    public void wrongAnswer() {

        if (!mButtonChoice1.getText().equals(mAnswer) || !mButtonChoice2.getText().equals(mAnswer) ||
                !mButtonChoice3.getText().equals(mAnswer) || !mButtonChoice4.getText().equals(mAnswer)) {

            if (mNumberOfQuestion >= 1 && mNumberOfQuestion < 6) {
                message.setText("נסה שוב");
                money.setText("0");
            } else if (mNumberOfQuestion > 5 && mNumberOfQuestion <= 10) {
                message.setText("עשית דרך יפה");
                money.setText("1,000");
            } else if (mNumberOfQuestion > 10 && mNumberOfQuestion <= 15) {
                message.setText("כל הכבוד");
                money.setText("32,000");
            }
        }
    }

    //----------------------------------------------------------------Win the game---------------------------------------------------------------------------------------
    @SuppressLint("SetTextI18n")
    public void winGame() {

        if (mNumberOfQuestion == 15) {
            progressBarTime.stopTimer();
            message.setText("אלוף האלופים");
            money.setText("1,000,000");
            showPopup();
        }
    }

    //-------------------------------------------------------Reset timer---------------------------------------------------------------------------------------------------
    public void resetTimer() {
        if (progressBarTime != null) {
            progressBarTime.cancelLongPress();
            progressBarTime.startTimer();
        }
    }

    //-------------------------------------------------------Flip layout change question----------------------------------------------------------------------------------
    private void flipView()
    {
        View rootLayout = findViewById(R.id.root);

        FlipAnimation flipAnimation = new FlipAnimation(rootLayout , rootLayout );

        if (rootLayout.getVisibility() == View.GONE)
        {
            flipAnimation.reverse();
        }
        rootLayout.startAnimation(flipAnimation);
    }
}