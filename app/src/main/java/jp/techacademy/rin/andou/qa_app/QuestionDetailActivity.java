package jp.techacademy.rin.andou.qa_app;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.valueOf;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    FloatingActionButton fab2;
    private DatabaseReference mAnswerRef;

    //変数
    boolean isFavorite;






    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");

            String name = (String) map.get("name");
            String uid = (String) map.get("uid");


            Answer answer = new Answer(body,name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    private ChildEventListener mFavalitListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            isFavorite = true;
            fab2.setImageResource(R.drawable.plus2);
    }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //ユーザー
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }


                //登録する。
                DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                DatabaseReference genreRef2 = dataBaseReference.child(Const.FavoritePATH).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(mQuestion.getQuestionUid());


                if (isFavorite) {
                    // お気に入りされている＝削除
                    Snackbar.make(view, "お気に入りが解除された", Snackbar.LENGTH_LONG).show();
                    genreRef2.removeValue();
                    isFavorite=false;

                    //クリックされたら画像を変える。
                    fab2.setImageResource(R.drawable.plus3);

                } else {
                    // お気に入りされていない＝登録
                    Snackbar.make(view, "お気に入りが登録された", Snackbar.LENGTH_LONG).show();
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("genre",valueOf(mQuestion.getGenre()) );
                    genreRef2.setValue(data);
                    isFavorite=true;

                    //クリックされたら画像を変える。
                    fab2.setImageResource(R.drawable.plus2);
                }

            }
        });

        //登録する。
        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();


        DatabaseReference genreRef2 = dataBaseReference.child(Const.FavoritePATH).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(mQuestion.getQuestionUid());

        genreRef2.addChildEventListener(mFavalitListener);


        //DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);






    }

}