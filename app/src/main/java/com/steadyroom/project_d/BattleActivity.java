package com.steadyroom.project_d;

import android.content.Intent;
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

import java.util.HashMap;
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

    private Map<String, String> currentActions = new HashMap<>();


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
            selectAction("attack");
        });

        btnSkill.setOnClickListener(v -> {
            if (mySkillLeft <= 0) {
                Toast.makeText(this, "스킬 횟수 모두 사용했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            selectAction("skill");
        });
    }

    private void listenRoomDataChanges() { //전투 데이터 실시간 반영
        roomListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                Map<String, Object> roomData = (Map<String, Object>) snapshot.getValue();
                if (roomData == null) return;

                Map<String, Object> players = (Map<String, Object>) roomData.get("players");
                if (players == null) return;

                findOpponentUid(players);

                // 플레이어 상태 업데이트
                for (Map.Entry<String, Object> entry : players.entrySet()) {
                    String uid = entry.getKey();
                    Map<String, Object> data = (Map<String, Object>) entry.getValue();
                    updatePlayerStatus(data, uid.equals(myUid));
                }

                // 동시 선택 행동 읽기
                Map<String, String> actions = new HashMap<>();
                if (roomData.get("actions") instanceof Map) {
                    Map<String, Object> rawActions = (Map<String, Object>) roomData.get("actions");
                    for (String key : rawActions.keySet()) {
                        Object val = rawActions.get(key);
                        if (val instanceof String) {
                            actions.put(key, (String) val);
                        }
                    }
                }

                if (actions.containsKey(myUid) && actions.containsKey(opponentUid)) {
                    if (myUid.compareTo(opponentUid) < 0) {
                        processTurn(actions);
                    }
                } else {
                    // 행동 대기 중 UI 처리
                    if (actions.containsKey(myUid)) {
                        tvTurn.setText("상대 입력 대기 중...");
                        disableBattleInputs();
                    } else {
                        tvTurn.setText("행동 선택하세요");
                        updateButtonsClickable();
                    }
                }

                // 경기 종료 처리
                String state = (String) roomData.get("state");
                if (!isBattleEnded && "finished".equals(state)) {
                    isBattleEnded = true;
                    disableBattleInputs();

                    Map<String, Object> result = (Map<String, Object>) roomData.get("result");
                    if (result != null) {
                        String myResult = (String) result.get(myUid);
                        if (myResult != null) {
                            showBattleResultDialog("win".equals(myResult));
                        }
                    }
                    //return;
                }
            }


            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(BattleActivity.this, "데이터 로드 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        roomRef.addValueEventListener(roomListener);

    }

    private void processTurn(Map<String, String> actions) {
        String myAction = actions.get(myUid);
        String oppAction = actions.get(opponentUid);

        // 결과 계산 예시 (간단한 공격-방어 판정)
        int damageToOpponent = 0;
        int damageToMe = 0;

        if ("attack".equals(myAction)) {
            if ("defense".equals(oppAction)) damageToOpponent = 0;  // 방어 성공
            else damageToOpponent = myAttack;
        } else if ("skill".equals(myAction)) {
            if ("defense".equals(oppAction)) damageToOpponent = myDirt / 2; // 스킬은 반감
            else damageToOpponent = myDirt;
            mySkillLeft = Math.max(0, mySkillLeft - 1);
        } else {
            damageToOpponent = 0;
        }

        // 상대 공격 처리 (mirror)
        if ("attack".equals(oppAction)) {
            if ("defense".equals(myAction)) damageToMe = 0;
            else damageToMe = opponentAttack;
        } else if ("skill".equals(oppAction)) {
            if ("defense".equals(myAction)) damageToMe = opponentDirt / 2;
            else damageToMe = opponentDirt;
            opponentSkillLeft = Math.max(0, opponentSkillLeft - 1);
        } else{
            damageToMe = 0;
        }

        // DB 트랜잭션으로 HP 및 스킬횟수 업데이트, actions 제거하며 턴 넘김
        int finalDamageToOpponent = damageToOpponent;
        int finalDamageToMe = damageToMe;
        roomRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Map<String, Object> rd = (Map<String, Object>) currentData.getValue();
                if (rd == null) return Transaction.abort();

                Map<String, Object> players = (Map<String, Object>) rd.get("players");
                if (players == null) return Transaction.abort();

                Map<String, Object> myData = (Map<String, Object>) players.get(myUid);
                Map<String, Object> opData = (Map<String, Object>) players.get(opponentUid);
                if (myData == null || opData == null) return Transaction.abort();

                int opHp = getIntFromObject(opData.get("hp"));
                int myHp = getIntFromObject(myData.get("hp"));

                opHp = Math.max(0, opHp - finalDamageToOpponent);
                myHp = Math.max(0, myHp - finalDamageToMe);

                opData.put("hp", opHp);
                myData.put("hp", myHp);
                myData.put("skillLeft", mySkillLeft);
                opData.put("skillLeft", opponentSkillLeft);

                players.put(myUid, myData);
                players.put(opponentUid, opData);

                rd.put("players", players);

                // actions 초기화하여 다음 턴 준비
                rd.put("actions", null);

                // 종료 체크
                if (opHp == 0 || myHp == 0) {
                    rd.put("state", "finished");
                    Map<String, Object> result = (Map<String, Object>) rd.get("result");
                    if (result == null) result = new java.util.HashMap<>();
                    if (opHp == 0) {
                        result.put(myUid, "win");
                        result.put(opponentUid, "lose");
                    } else {
                        result.put(myUid, "lose");
                        result.put(opponentUid, "win");
                    }
                    rd.put("result", result);
                }

                currentData.setValue(rd);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (!committed) {
                    runOnUiThread(() -> Toast.makeText(BattleActivity.this, "턴 처리 실패:" + (error != null ? error.getMessage() : ""), Toast.LENGTH_SHORT).show());
                } else {
                    updateTurnUI();
                }
            }
        });
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
        tvTurn.setText("행동을 선택해주십시오.");
    }

    private void selectAction(String actionType) {
        roomRef.child("actions").child(myUid).setValue(actionType);
        disableBattleInputs();
        tvTurn.setText("상대 입력 대기 중...");
    }

    private void updateButtonsClickable() { //버튼 활성화
        btnAttack.setEnabled(true);
        btnSkill.setEnabled(mySkillLeft > 0);
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

    private void disableBattleInputs() { //버튼 비활성화
        btnAttack.setEnabled(false);
        btnSkill.setEnabled(false);
    }

    private void showBattleResultDialog(boolean isWin) { //결과 화면 출력
        String message = isWin ? "승리하셨습니다!" : "패배하셨습니다!";

        updateScore(isWin);

        new AlertDialog.Builder(this)
                .setTitle("전투 종료")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("확인", (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(BattleActivity.this, RankingActivity.class);
                    startActivity(intent);
                    finish();
                })
                .show();


    }

    private void updateScore(boolean win) { //RankingPoint
        DatabaseReference rankingPointRef = FirebaseDatabase.getInstance()
                .getReference("users").child(myUid).child("rankingPoint");
        int pointChange = win ? 15 : -10;

        rankingPointRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Integer currentValue = currentData.getValue(Integer.class);
                int currentPoint = currentValue == null ? 0 : currentValue;
                int newPoint = Math.max(0, currentPoint + pointChange);
                currentData.setValue(newPoint);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (committed && snapshot != null && snapshot.exists()) {
                    int updatedPoint = snapshot.getValue(Integer.class);
                    FirebaseDatabase.getInstance().getReference("rankings")
                            .child(myUid)
                            .setValue(updatedPoint);
                }
            }
        });
    }


    private void surrender() {
        if (opponentUid == null) return; // 상대방 UID 없으면 무시

        roomRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Map<String, Object> roomData = (Map<String, Object>) currentData.getValue();
                if (roomData == null) return Transaction.abort();

                // 이미 끝난 경기면 패스
                if ("finished".equals(roomData.get("state"))) return Transaction.abort();

                Map<String, Object> result = (Map<String, Object>) roomData.get("result");
                if (result == null) result = new java.util.HashMap<>();

                result.put(myUid, "lose");
                result.put(opponentUid, "win");

                roomData.put("state", "finished");
                roomData.put("result", result);

                currentData.setValue(roomData);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (committed) {
                    isBattleEnded = true;
                    runOnUiThread(() -> {
                        Toast.makeText(BattleActivity.this, "항복 처리되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 전투가 끝나지 않았고, 단순 회전이 아닐 때 = 도중에 나간 경우
        if (!isBattleEnded) {
            surrender();
        }
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

