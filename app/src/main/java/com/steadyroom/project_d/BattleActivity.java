package com.steadyroom.project_d;

import android.content.Intent;
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
    private TextView tvMyAttack, tvMyHp, tvMyDirt, tvMySkill, tvMyAction;
    private TextView tvOpponentAttack, tvOpponentHp, tvOpponentDirt, tvOpponentSkill, tvOpponentAction;
    private TextView tvTurn;
    private Button btnAttack, btnSkill, btnHeal, btnCounter;

    private String roomId;
    private DatabaseReference roomRef;
    private ValueEventListener roomListener;
    private String myUid;
    private String opponentUid = null;

    private int myHp, opponentHp;
    private int myAttack, opponentAttack;
    private int myDirt, opponentDirt;
    private int mySkillPoint, opponentSkillPoint;

    //스킬 코스트
    private static final int COST_SKILL = 3;
    private static final int COST_HEAL = 2;
    private static final int COST_COUNTER = 1;
    private static final int COST_ATTACK = 0;



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
        tvMySkill = findViewById(R.id.tv_my_skill_count);
        tvMyAction = findViewById(R.id.tv_my_action);
        tvOpponentAttack = findViewById(R.id.tv_opponent_attack);
        tvOpponentHp = findViewById(R.id.tv_opponent_hp);
        tvOpponentDirt = findViewById(R.id.tv_opponent_dirt);
        tvOpponentSkill = findViewById(R.id.tv_opponent_skill_count);
        tvOpponentAction = findViewById(R.id.tv_opponent_action);
        tvTurn = findViewById(R.id.tv_turn);
        btnAttack = findViewById(R.id.btn_attack);
        btnSkill = findViewById(R.id.btn_skill);
        btnHeal = findViewById(R.id.btn_heal);
        btnCounter = findViewById(R.id.btn_counter);
    }

    private void setupInitialUI() { //UI 기본값 세팅
        tvMyNickname.setText("");
        tvOpponentNickname.setText("");

        tvMyCharacterName.setText("");
        tvOpponentCharacterName.setText("");

        tvMySkill.setText("스킬 횟수: 10/10");
        tvOpponentSkill.setText("상대 스킬: 10/10");

        btnAttack.setEnabled(false);
        btnSkill.setEnabled(false);
        btnHeal.setEnabled(false);
        btnCounter.setEnabled(false);

        tvTurn.setText("로딩 중...");
    }

    private void setupListeners() { //리스너 세팅
        btnAttack.setOnClickListener(v -> {
            selectAction("attack");
        });

        btnSkill.setOnClickListener(v -> {
            if (mySkillPoint < COST_SKILL) {
                Toast.makeText(this, "스킬 포인트가 부족합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            selectAction("skill");
        });

        btnHeal.setOnClickListener(v -> {
            if (mySkillPoint < COST_HEAL) {
                Toast.makeText(this, "스킬 포인트가 부족합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            selectAction("heal");
        });

        btnCounter.setOnClickListener(v -> {
            if (mySkillPoint < COST_COUNTER) {
                Toast.makeText(this, "스킬 포인트가 부족합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            selectAction("counter");
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
                    showResult(roomData, 2000L); // 3초 지연
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

        // 준비: 누적 버퍼
        int dmgToOppCounter = 0, dmgToMeCounter = 0;
        int dmgToOppAction = 0,  dmgToMeAction  = 0;
        int myHpDelta = 0, oppHpDelta = 0; // 회복은 +, 피해는 음수로 처리해도 됨

        switch (myAction) {
            case "attack": dmgToOppAction += myAttack; break;
            case "skill":  dmgToOppAction += myDirt; break;
            case "heal":   myHpDelta += myDirt/2; break;
            case "counter":  break;
        }

        switch (oppAction) {
            case "attack": dmgToMeAction += opponentAttack; break;
            case "skill":  dmgToMeAction += opponentDirt;  break;
            case "heal":   oppHpDelta += opponentDirt/2; break;
            case "counter":  break;
        }

        //카운터 고려
        if ("counter".equals(myAction) && ("attack".equals(oppAction) || "skill".equals(oppAction))) {
            double r = Math.random();
            if (r < 0.5) {
                dmgToOppCounter += dmgToMeAction * 1.5;
                dmgToMeAction = 0;
            }
            else {
                dmgToMeCounter += dmgToMeAction * 1.3;
                dmgToMeAction = 0;
            }
        }
        if ("counter".equals(oppAction) && ("attack".equals(myAction) || "skill".equals(myAction))) {
            double r = Math.random();
            if (r < 0.5) { //상대 카운터 성공
                dmgToMeCounter += dmgToOppAction * 1.5;
                dmgToOppAction = 0;
            }
            else { //실패
                dmgToOppCounter += dmgToOppAction * 1.3;
                dmgToOppAction = 0;
            }
        }


        int totalDmgToOpp = dmgToOppCounter + dmgToOppAction; // 상대가 받을 총 피해
        int totalDmgToMe  = dmgToMeCounter  + dmgToMeAction;  // 내가 받을 총 피해
        int totalHealMe   = myHpDelta;                        // 내가 받을 총 회복
        int totalHealOpp  = oppHpDelta;                       // 상대가 받을 총 회복
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

                //체력 계산
                int opHp = getIntFromObject(opData.get("hp"));
                int myHp = getIntFromObject(myData.get("hp"));
                opHp = Math.max(0, opHp + totalHealOpp - totalDmgToOpp);
                myHp = Math.max(0, myHp + totalHealMe - totalDmgToMe);

                //스킬 포인트 계산
                int mySkillPoint = getIntFromObject(myData.get("skillPoint")); //남은 스킬 포인트
                int oppSkillPoint = getIntFromObject(opData.get("skillPoint"));
                int mySkillCost  = costOf(actions.get(myUid)); //사용 스킬 포인트
                int oppSkillCost = costOf(actions.get(opponentUid));

                if (mySkillPoint < mySkillCost || oppSkillPoint < oppSkillCost) {
                    return Transaction.abort(); // 오류 방지
                }

                mySkillPoint  -= mySkillCost;
                oppSkillPoint -= oppSkillCost;

                mySkillPoint  = Math.min(10, mySkillPoint + 1);
                oppSkillPoint = Math.min(10, oppSkillPoint + 1);


                opData.put("hp", opHp);
                myData.put("hp", myHp);
                myData.put("skillPoint", mySkillPoint);
                opData.put("skillPoint", oppSkillPoint);
                myData.put("lastAction", actions.get(myUid));
                opData.put("lastAction", actions.get(opponentUid));

                players.put(myUid, myData);
                players.put(opponentUid, opData);

                rd.put("players", players);



                // actions 초기화하여 다음 턴 준비
                rd.put("actions", null);

                // 종료 체크
                if (opHp == 0 || myHp == 0) {
                    rd.put("state", "finished");
                    Map<String, Object> result = (Map<String, Object>) rd.get("result");
                    if (result == null) result = new HashMap<>();
                    if (opHp == 0 && myHp == 0) {
                        result.put(myUid, "draw");
                        result.put(opponentUid, "draw");
                    } else if (opHp == 0) {
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
        int skillPoint = getIntFromObject(data.getOrDefault("skillPoint", 10));

        String nickname = (String) data.get("nickname");
        String characterName = (String) data.get("characterName");
        String action = (String) data.get("lastAction");

        if (isMyself) {
            myHp = hp; myAttack = attack; myDirt = dirt; mySkillPoint = skillPoint;
            if (nickname != null) tvMyNickname.setText(nickname);
            if (characterName != null) tvMyCharacterName.setText(characterName);
            tvMyHp.setText("HP: " + hp);
            tvMyAttack.setText("공격력: " + attack);
            tvMyDirt.setText("Dirt: " + dirt);
            tvMySkill.setText("스킬 포인트: " + skillPoint + "/10");
            if(action != null) tvMyAction.setText("내 행동: " + mapActionLabel(action));

        } else {
            opponentHp = hp; opponentAttack = attack; opponentDirt = dirt; opponentSkillPoint = skillPoint;
            if (nickname != null) tvOpponentNickname.setText(nickname);
            if (characterName != null) tvOpponentCharacterName.setText(characterName);
            tvOpponentHp.setText("HP: " + hp);
            tvOpponentAttack.setText("공격력: " + attack);
            tvOpponentDirt.setText("Dirt: " + dirt);
            tvOpponentSkill.setText("스킬 포인트: " + skillPoint + "/10");
            if(action != null) tvOpponentAction.setText("상대 행동: " + mapActionLabel(action));
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

    private String mapActionLabel(String a){
        if (a == null) return "시작";
        switch (a){
            case "attack": return "기본 공격";
            case "skill":  return "스킬";
            case "heal":   return "회복";
            case "counter":return "반격";
            default:       return a;
        }
    }

    private int costOf(String action) {
        if (action == null) return 0;
        switch (action) {
            case "skill":   return COST_SKILL;
            case "heal":    return COST_HEAL;
            case "counter": return COST_COUNTER;
            case "attack":  return COST_ATTACK;
            default:        return 0;
        }
    }

    private void updateButtonsClickable() { //버튼 활성화
        btnAttack.setEnabled(true);
        btnSkill.setEnabled(mySkillPoint >= COST_SKILL);
        btnHeal.setEnabled(mySkillPoint >= COST_HEAL);
        btnCounter.setEnabled(mySkillPoint >= COST_COUNTER);
    }

    private void disableBattleInputs() { //버튼 비활성화
        btnAttack.setEnabled(false);
        btnSkill.setEnabled(false);
        btnHeal.setEnabled(false);
        btnCounter.setEnabled(false);
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


    private void showResult(Map<String,Object> roomData, long delayMs) {
        Object resObj = roomData.get("result");
        String myResult = (resObj instanceof Map && ((Map<?,?>) resObj).get(myUid) instanceof String)
                ? (String) ((Map<?,?>) resObj).get(myUid)
                : "draw";

        tvTurn.setText("전투 종료... 결과 계산 중");
        tvTurn.postDelayed(() -> showBattleResultDialog(myResult), delayMs);
    }

    private void showBattleResultDialog(String myResult) {
        String title = "전투 종료";
        String message;
        switch (myResult) {
            case "win":  message = "승리하셨습니다!"; break;
            case "lose": message = "패배하셨습니다!"; break;
            case "draw": message = "무승부입니다!";   break;
            default:     message = "전투가 종료되었습니다."; break;
        }

        // 점수 반영
        updateScore(myResult);

        new AlertDialog.Builder(this)
                .setTitle(title)
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

    private void updateScore(String myResult) {
        int pointChange;
        if ("win".equals(myResult))      pointChange = 15;
        else if ("lose".equals(myResult))pointChange = -10;
        else                             pointChange = 0;

        DatabaseReference rankingPointRef = FirebaseDatabase.getInstance()
                .getReference("users").child(myUid).child("rankingPoint");

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
                    Integer updated = snapshot.getValue(Integer.class);
                    int updatedPoint = updated == null ? 0 : updated;
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
                if (result == null) result = new HashMap<>();

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

