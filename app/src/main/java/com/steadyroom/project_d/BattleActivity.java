package com.steadyroom.project_d;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class BattleActivity extends AppCompatActivity {

    private ImageView ivMyCharacter, ivOpponentCharacter;
    private TextView tvMyNickname, tvOpponentNickname;
    private TextView tvMyCharacterName, tvOpponentCharacterName;
    private TextView tvMyAttack, tvMyHp, tvMyDirt, tvMySkill;
    private TextView tvOpponentAttack, tvOpponentHp, tvOpponentDirt, tvOpponentSkill;
    private TextView tvTurn;
    private Button btnAttack, btnSkill;

    private String roomId;
    private DatabaseReference roomRef;
    private ValueEventListener roomListener;
    private String myUid;
    private String opponentUid = null;

    private int myHp, opponentHp;
    private int myAttack, opponentAttack;
    private int myDirt, opponentDirt;
    private int mySkillLeft, opponentSkillLeft;

    private String currentTurnUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_battle);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        roomId = getIntent().getStringExtra("roomId");
        roomRef = FirebaseDatabase.getInstance().getReference("battleRooms").child(roomId);

        // 뷰 바인딩
        bindViews();

        // 기본 초기화 및 UI 세팅
        setupInitialUI();

        // 버튼 이벤트 설정
        setupListeners();

        //룸 데이터 실시간 감시
        listenRoomDataChanges();
    }

    private void bindViews() {
        ivMyCharacter = findViewById(R.id.iv_my_character);
        ivOpponentCharacter = findViewById(R.id.iv_opponent_character);
        tvMyNickname = findViewById(R.id.tv_my_nickname);
        tvOpponentNickname = findViewById(R.id.tv_opponent_nickname);
        tvMyCharacterName = findViewById(R.id.tv_my_character_name);
        tvOpponentCharacterName = findViewById(R.id.tv_opponent_character_name);
        tvMyAttack = findViewById(R.id.tv_my_attack);
        tvMyHp = findViewById(R.id.tv_my_hp);
        tvMyDirt = findViewById(R.id.tv_my_dirt);
        tvMySkill = findViewById(R.id.tv_my_skill);
        tvOpponentAttack = findViewById(R.id.tv_opponent_attack);
        tvOpponentHp = findViewById(R.id.tv_opponent_hp);
        tvOpponentDirt = findViewById(R.id.tv_opponent_dirt);
        tvOpponentSkill = findViewById(R.id.tv_opponent_skill_count);
        tvTurn = findViewById(R.id.tv_turn);
        btnAttack = findViewById(R.id.btn_attack);
        btnSkill = findViewById(R.id.btn_skill);
    }

    private void setupInitialUI() { //UI 기본값 세팅
        tvMyNickname.setText("");
        tvOpponentNickname.setText("");

        tvMyCharacterName.setText("");
        tvOpponentCharacterName.setText("");

        tvMySkill.setText("스킬 횟수: 3/3");
        tvOpponentSkill.setText("상대 스킬: 3/3");

        btnAttack.setEnabled(false);
        btnSkill.setEnabled(false);

        tvTurn.setText("로딩 중...");
    }

    private void setupListeners() { //리스너 세팅
        btnAttack.setOnClickListener(v -> {
            if (!isMyTurn()) return;
            performBasicAttack();
        });

        btnSkill.setOnClickListener(v -> {
            if (!isMyTurn()) return;
            if (mySkillLeft <= 0) {
                Toast.makeText(this, "스킬 횟수 모두 사용했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            performSkillAttack();
        });
    }

    private boolean isMyTurn() { //턴 확인
        return myUid.equals(currentTurnUid);
    }

    private void listenRoomDataChanges() { //전투 데이터 실시간 반영
        roomListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                Map<String, Object> roomData = (Map<String, Object>) snapshot.getValue();
                if (roomData == null) return;

                currentTurnUid = (String) roomData.get("turn");

                Map<String, Object> players = (Map<String, Object>) roomData.get("players");
                if (players == null) return;

                findOpponentUid(players);

                for (Map.Entry<String, Object> entry : players.entrySet()) {
                    String uid = entry.getKey();
                    Map<String, Object> data = (Map<String, Object>) entry.getValue();
                    updatePlayerStatus(data, uid.equals(myUid));
                }

//                Map res = (Map) roomData.get("result");
//                if (!isBattleEnded && "finished".equals(roomData.get("state"))) {
//                    isBattleEnded = true;
//                    disableBattleInputs();
//                    if (res != null) {
//                        String myResult = (String) res.get(myUid);
//                        if (myResult != null) showBattleResultDialog("win".equals(myResult));
//                    }
//                    return;
//                }

                updateTurnUI();
                updateButtonsClickable();
                checkBattleEnd();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(BattleActivity.this, "데이터 로드 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        roomRef.addValueEventListener(roomListener);

    }

    private void updatePlayerStatus(Map<String, Object> data, boolean isMyself) {
        int hp = getIntFromObject(data.get("hp"));
        int attack = getIntFromObject(data.get("attack"));
        int dirt = getIntFromObject(data.get("dirt"));
        int skillLeft = getIntFromObject(data.getOrDefault("skillLeft", 3));

        String nickname = (String) data.get("nickname");
        String characterName = (String) data.get("characterName");

        if (isMyself) {
            myHp = hp; myAttack = attack; myDirt = dirt; mySkillLeft = skillLeft;
            if (nickname != null) tvMyNickname.setText(nickname);
            if (characterName != null) tvMyCharacterName.setText(characterName);
            tvMyHp.setText("HP: " + hp);
            tvMyAttack.setText("공격력: " + attack);
            tvMyDirt.setText("Dirt: " + dirt);
            tvMySkill.setText("스킬 횟수: " + skillLeft + "/3");

        } else {
            opponentHp = hp; opponentAttack = attack; opponentDirt = dirt; opponentSkillLeft = skillLeft;
            if (nickname != null) tvOpponentNickname.setText(nickname);
            if (characterName != null) tvOpponentCharacterName.setText(characterName);
            tvOpponentHp.setText("HP: " + hp);
            tvOpponentAttack.setText("공격력: " + attack);
            tvOpponentDirt.setText("Dirt: " + dirt);
            tvOpponentSkill.setText("상대 스킬: " + skillLeft + "/3");
        }
    }

    private int getIntFromObject(Object obj) { //형 변환
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private void updateTurnUI() { //현재 턴 확인
        if (isMyTurn()) {
            tvTurn.setText("내 턴");
            tvTurn.setTextColor(Color.BLUE);
        } else {
            tvTurn.setText("상대 턴");
            tvTurn.setTextColor(Color.RED);
        }
    }

    private void updateButtonsClickable() { //버튼 활성화
        boolean enabled = isMyTurn();
        btnAttack.setEnabled(enabled);
        btnSkill.setEnabled(enabled && mySkillLeft > 0);
    }

    private void performBasicAttack() {
        // 샘플 대미지 15 고정
        int damage = myAttack;
        dealDamageToOpponent(damage);
    }

    private void performSkillAttack() {
        if (mySkillLeft <= 0) return;

        int skillDamage = myDirt;
        dealDamageToOpponent(skillDamage, true); // true는 스킬 공격임을 표시
    }

    private void dealDamageToOpponent(int damage) { //일반 공격
        dealDamageToOpponent(damage, false);
    }


    private void dealDamageToOpponent(int damage, boolean isSkill) { //스킬 공격
        roomRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {

                Map<String, Object> roomData = (Map<String, Object>) currentData.getValue();
                if (roomData == null) return Transaction.abort();

                String turn = (String) roomData.get("turn");
                if (turn == null || !turn.equals(myUid)) return Transaction.abort();

                Map<String, Object> players = (Map<String, Object>) roomData.get("players");
                if (players == null) return Transaction.abort();

                if (opponentUid == null || !players.containsKey(opponentUid)) return Transaction.abort();
                if (!players.containsKey(myUid)) return Transaction.abort();

                Map<String, Object> opponentData = (Map<String, Object>) players.get(opponentUid);
                Map<String, Object> myData = (Map<String, Object>) players.get(myUid);

                if (opponentData == null || myData == null) return Transaction.abort();

                int beforeHp = getIntFromObject(opponentData.get("hp"));
                int afterHp = Math.max(0, beforeHp - damage);
                opponentData.put("hp", afterHp);
                players.put(opponentUid, opponentData);

                if (isSkill) {
                    int skillLeftBefore = getIntFromObject(myData.getOrDefault("skillLeft", 0));
                    int skillLeftAfter = Math.max(0, skillLeftBefore - 1);
                    myData.put("skillLeft", skillLeftAfter);
                    players.put(myUid, myData);
                }

                if (afterHp <= 0) {
                    roomData.put("state", "finished");
                    Map<String, Object> result = (Map<String, Object>) roomData.get("result");
                    if (result == null) result = new java.util.HashMap<>();
                    result.put(myUid, "win");
                    result.put(opponentUid, "lose");
                    roomData.put("result", result);
                    currentData.setValue(roomData);
                    return Transaction.success(currentData);
                }

                // 턴 변경
                roomData.put("turn", opponentUid);
                currentData.setValue(roomData);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (!committed) {
                    Toast.makeText(BattleActivity.this, "공격 처리 실패: " + (error != null ? error.getMessage() : ""), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void findOpponentUid(Map<String, Object> players) {
        if (opponentUid == null) {
            for (String uid : players.keySet()) {
                if (!uid.equals(myUid)) {
                    opponentUid = uid;
                    break;
                }
            }
        }
    }
    private String getOpponentUid() { //상대 Uid 사용시 활용
        return opponentUid;
    }

    Boolean isBattleEnded = false;
    private void checkBattleEnd() {
        if (isBattleEnded) return;

        if (opponentHp <= 0) {
            isBattleEnded = true;
            // 승리
            showBattleResultDialog(true);
            disableBattleInputs();
            updateRoomStateFinished("win");
        } else if (myHp <= 0) {
            isBattleEnded = true;
            // 패배
            showBattleResultDialog(false);
            disableBattleInputs();
            updateRoomStateFinished("lose");
        }
    }

    private void disableBattleInputs() { //버튼 비활성화
        btnAttack.setEnabled(false);
        btnSkill.setEnabled(false);
    }

    private void showBattleResultDialog(boolean isWin) { //결과 화면 출력
        String message = isWin ? "승리하셨습니다!" : "패배하셨습니다!";

        new AlertDialog.Builder(this)
                .setTitle("전투 종료")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("확인", (dialog, which) -> {
                    dialog.dismiss();
                    finish();  // 액티비티 종료, 또는 다음 화면 이동 로직 추가
                })
                .show();
    }

    private void updateRoomStateFinished(String result) { //데이터 베이스 결과 업데이트
        roomRef.child("state").setValue("finished");
        roomRef.child("result").child(myUid).setValue(result);
        roomRef.child("result").child(opponentUid).setValue(result.equals("win") ? "lose" : "win");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (roomListener != null) {
            roomRef.removeEventListener(roomListener);
            roomListener = null;
        }
    }
}

