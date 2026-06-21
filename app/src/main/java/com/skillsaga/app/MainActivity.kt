package com.skillsaga.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkillSagaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BattleScreen()
                }
            }
        }
    }
}

@Composable
fun SkillSagaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC5),
            background = Color(0xFFF5F5F5),
            surface = Color.White
        ),
        content = content
    )
}

@Composable
fun BattleScreen() {
    val gameEngine = remember { GameEngine() }
    var battleState by remember { mutableStateOf(gameEngine.getBattleState()) }
    var selectedCharacter by remember { mutableStateOf<Character?>(null) }
    var selectedSkill by remember { mutableStateOf<Skill?>(null) }
    var selectedTarget by remember { mutableStateOf<Character?>(null) }
    var showTargetSelection by remember { mutableStateOf(false) }
    var showSkillSelection by remember { mutableStateOf(false) }
    var showVictoryDialog by remember { mutableStateOf(false) }
    var showDefeatDialog by remember { mutableStateOf(false) }

    // Initialize game
    LaunchedEffect(Unit) {
        gameEngine.initializeGame()
        battleState = gameEngine.getBattleState()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "技能传说 - 战斗",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Turn info
        Text(
            text = "第 ${battleState.currentTurn} 回合 | ${if (battleState.isPlayerTurn) "你的回合" else "敌方回合"}",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Game log
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            items(battleState.gameLog.takeLast(10)) { logEntry ->
                Text(
                    text = logEntry,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Player team status
        Text(
            text = "你的队伍",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            battleState.playerTeam.forEach { character ->
                CharacterCard(
                    character = character,
                    isSelected = selectedCharacter == character,
                    onClick = {
                        if (character.hp > 0) {
                            selectedCharacter = character
                            showSkillSelection = true
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Enemy team status
        Text(
            text = "敌方队伍",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            battleState.enemyTeam.forEach { character ->
                EnemyCard(
                    character = character,
                    isSelected = selectedTarget == character,
                    onClick = {
                        if (selectedSkill != null && character.hp > 0) {
                            selectedTarget = character
                            // Execute the skill
                            gameEngine.executeSkill(selectedCharacter!!, selectedSkill!!, character)
                            selectedSkill = null
                            selectedTarget = null
                            showSkillSelection = false
                            
                            // Check for game end
                            if (gameEngine.checkGameEnd()) {
                                if (battleState.playerTeam.all { it.hp <= 0 }) {
                                    showDefeatDialog = true
                                } else {
                                    showVictoryDialog = true
                                }
                            } else {
                                // AI turn
                                gameEngine.aiTurn()
                                battleState = gameEngine.getBattleState()
                                
                                // Start new turn
                                gameEngine.startNewTurn()
                                battleState = gameEngine.getBattleState()
                            }
                        }
                    }
                )
            }
        }

        // Skill selection dialog
        if (showSkillSelection && selectedCharacter != null) {
            AlertDialog(
                onDismissRequest = { showSkillSelection = false },
                title = { Text("选择技能 - ${selectedCharacter!!.name}") },
                text = {
                    Column {
                        selectedCharacter!!.skills.forEach { skill ->
                            Button(
                                onClick = {
                                    selectedSkill = skill
                                    showSkillSelection = false
                                    showTargetSelection = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                enabled = canUseSkill(selectedCharacter!!, skill)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(skill.name, fontWeight = FontWeight.Bold)
                                    Text(skill.description, fontSize = 12.sp)
                                    Text("消耗: ${skill.apCost} AP", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showSkillSelection = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // Target selection
        if (showTargetSelection && selectedSkill != null) {
            AlertDialog(
                onDismissRequest = { showTargetSelection = false },
                title = { Text("选择目标") },
                text = {
                    Column {
                        Text("使用 ${selectedSkill!!.name} 攻击：")
                        Spacer(modifier = Modifier.height(8.dp))
                        battleState.enemyTeam.filter { it.hp > 0 }.forEach { enemy ->
                            Button(
                                onClick = {
                                    selectedTarget = enemy
                                    showTargetSelection = false
                                    // Execute the skill
                                    gameEngine.executeSkill(selectedCharacter!!, selectedSkill!!, enemy)
                                    selectedSkill = null
                                    selectedTarget = null
                                    
                                    // Check for game end
                                    if (gameEngine.checkGameEnd()) {
                                        if (battleState.playerTeam.all { it.hp <= 0 }) {
                                            showDefeatDialog = true
                                        } else {
                                            showVictoryDialog = true
                                        }
                                    } else {
                                        // AI turn
                                        gameEngine.aiTurn()
                                        battleState = gameEngine.getBattleState()
                                        
                                        // Start new turn
                                        gameEngine.startNewTurn()
                                        battleState = gameEngine.getBattleState()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text("${enemy.name} (HP: ${enemy.hp})")
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showTargetSelection = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // Victory dialog
        if (showVictoryDialog) {
            AlertDialog(
                onDismissRequest = { showVictoryDialog = false },
                title = { Text("胜利！") },
                text = { Text("恭喜你赢得了战斗！") },
                confirmButton = {
                    Button(onClick = {
                        showVictoryDialog = false
                        gameEngine.initializeGame()
                        battleState = gameEngine.getBattleState()
                    }) {
                        Text("重新开始")
                    }
                }
            )
        }

        // Defeat dialog
        if (showDefeatDialog) {
            AlertDialog(
                onDismissRequest = { showDefeatDialog = false },
                title = { Text("失败！") },
                text = { Text("你的队伍被击败了。") },
                confirmButton = {
                    Button(onClick = {
                        showDefeatDialog = false
                        gameEngine.initializeGame()
                        battleState = gameEngine.getBattleState()
                    }) {
                        Text("重新开始")
                    }
                }
            )
        }
    }
}

@Composable
fun CharacterCard(character: Character, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFBBDEFB) else Color.White
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = character.title,
                fontSize = 10.sp,
                color = Color.Gray
            )
            Text(
                text = character.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // HP bar
            LinearProgressIndicator(
                progress = { character.hp.toFloat() / character.maxHp },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (character.hp > character.maxHp * 0.3) Color.Green else Color.Red,
                trackColor = Color.LightGray
            )
            Text(
                text = "HP: ${character.hp}/${character.maxHp}",
                fontSize = 10.sp
            )
            
            // AP
            Text(
                text = "AP: ${character.ap}/${character.maxAp}",
                fontSize = 10.sp
            )
            
            // Shield
            if (character.shield > 0) {
                Text(
                    text = "护盾: ${character.shield}",
                    fontSize = 10.sp,
                    color = Color.Blue
                )
            }
            
            // Special resources
            if (character.momentum > 0) {
                Text(
                    text = "势: ${character.momentum}",
                    fontSize = 10.sp,
                    color = Color(0xFFFF9800)
                )
            }
            if (character.intent > 0) {
                Text(
                    text = "意: ${character.intent}",
                    fontSize = 10.sp,
                    color = Color(0xFF9C27B0)
                )
            }
        }
    }
}

@Composable
fun EnemyCard(character: Character, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFCDD2) else Color(0xFFF5F5F5)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = character.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (character.hp <= 0) Color.Gray else Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // HP bar
            LinearProgressIndicator(
                progress = { character.hp.toFloat() / character.maxHp },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (character.hp > character.maxHp * 0.3) Color.Green else Color.Red,
                trackColor = Color.LightGray
            )
            Text(
                text = "HP: ${character.hp}/${character.maxHp}",
                fontSize = 10.sp
            )
            
            if (character.hp <= 0) {
                Text(
                    text = "已击败",
                    fontSize = 10.sp,
                    color = Color.Red
                )
            }
        }
    }
}

private fun canUseSkill(character: Character, skill: Skill): Boolean {
    // Check AP cost
    if (character.ap < skill.apCost) return false
    
    // Check conditions
    if (skill.requiresCondition != null) {
        when (skill.requiresCondition) {
            "momentum>=10" -> if (character.momentum < 10) return false
            "intent>=3" -> if (character.intent < 3) return false
        }
    }
    
    return true
}