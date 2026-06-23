package com.skillsaga.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════
//  主题
// ═══════════════════════════════════════════════════════════

private val DarkBg = Color(0xFF0F0F1A)
private val CardBg = Color(0xFF1A1A2E)
private val CardBgLight = Color(0xFF252540)
private val AccentGold = Color(0xFFFFD700)
private val AccentPurple = Color(0xFF9C27B0)
private val AccentBlue = Color(0xFF2196F3)
private val AccentRed = Color(0xFFE53935)
private val AccentGreen = Color(0xFF4CAF50)
private val AccentOrange = Color(0xFFFF9800)
private val AccentCyan = Color(0xFF00BCD4)
private val TextPrimary = Color(0xFFE0E0E0)
private val TextSecondary = Color(0xFF9E9E9E)
private val HpGreen = Color(0xFF4CAF50)
private val HpYellow = Color(0xFFFFC107)
private val HpRed = Color(0xFFE53935)
private val ShieldBlue = Color(0xFF42A5F5)
private val ApGold = Color(0xFFFFD54F)

enum class Screen { MENU, BATTLE, VICTORY, DEFEAT, STAGE_CLEAR }

// ═══════════════════════════════════════════════════════════
//  MainActivity
// ═══════════════════════════════════════════════════════════

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkillSagaApp()
        }
    }
}

@Composable
fun SkillSagaApp() {
    var screen by remember { mutableStateOf(Screen.MENU) }
    var stage by remember { mutableStateOf(1) }
    val engine = remember { GameEngine() }
    var stateVersion by remember { mutableStateOf(0) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AccentPurple,
            secondary = AccentCyan,
            background = DarkBg,
            surface = CardBg,
            onSurface = TextPrimary
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = DarkBg) {
            when (screen) {
                Screen.MENU -> MenuScreen(onStart = { stage = 1; screen = Screen.BATTLE })
                Screen.BATTLE -> BattleScreen(
                    engine = engine,
                    stage = stage,
                    onVictory = { screen = Screen.STAGE_CLEAR },
                    onDefeat = { screen = Screen.DEFEAT },
                    onMenu = { screen = Screen.MENU },
                    onStateChange = { stateVersion++ }
                )
                Screen.STAGE_CLEAR -> StageClearScreen(
                    stage = stage,
                    onNext = {
                        stage++
                        engine.nextStage()
                        screen = Screen.BATTLE
                    },
                    onMenu = { screen = Screen.MENU }
                )
                Screen.VICTORY -> ResultScreen(true, onMenu = { screen = Screen.MENU })
                Screen.DEFEAT -> ResultScreen(false, onMenu = { screen = Screen.MENU })
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  主菜单
// ═══════════════════════════════════════════════════════════

@Composable
fun MenuScreen(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A0A2E), Color(0xFF0F0F1A), Color(0xFF0A0A1A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Text("⚔️ 技能传说 ⚔️", fontSize = 42.sp, fontWeight = FontWeight.Black, color = AccentGold)
            Spacer(Modifier.height(8.dp))
            Text("SkillSaga", fontSize = 18.sp, color = TextSecondary, letterSpacing = 8.sp)
            Spacer(Modifier.height(48.dp))

            // 角色预览
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CharacterPreviewCard("🌊", "剑王·大海", "DPS", "势能成长", AccentBlue)
                CharacterPreviewCard("🌙", "隐月·薇", "刺客", "暴击爆发", AccentPurple)
                CharacterPreviewCard("⛰️", "破军·岩", "坦克", "护盾辅助", Color(0xFF8D6E63))
            }
            Spacer(Modifier.height(48.dp))

            // 开始按钮
            Button(
                onClick = onStart,
                modifier = Modifier
                    .width(240.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("开始战斗", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Text("3 关挑战 · 回合制策略战斗", fontSize = 14.sp, color = TextSecondary)
        }
    }
}

@Composable
fun CharacterPreviewCard(emoji: String, name: String, role: String, desc: String, color: Color) {
    Card(
        modifier = Modifier.size(100.dp, 140.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgLight),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 36.sp)
            Spacer(Modifier.height(4.dp))
            Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
            Text(role, fontSize = 10.sp, color = TextSecondary)
            Text(desc, fontSize = 9.sp, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  战斗界面
// ═══════════════════════════════════════════════════════════

@Composable
fun BattleScreen(
    engine: GameEngine,
    stage: Int,
    onVictory: () -> Unit,
    onDefeat: () -> Unit,
    onMenu: () -> Unit,
    onStateChange: () -> Unit
) {
    var initialized by remember { mutableStateOf(false) }
    var battleState by remember { mutableStateOf(BattleState(emptyList(), emptyList())) }
    var showSkills by remember { mutableStateOf(false) }
    var selectedSkill by remember { mutableStateOf<SkillDef?>(null) }
    var showLog by remember { mutableStateOf(false) }
    var processing by remember { mutableStateOf(false) }

    // 初始化
    LaunchedEffect(stage, initialized) {
        if (!initialized) {
            engine.initializeGame(stage)
            battleState = engine.getBattleState()
            initialized = true
            // 如果第一个行动的是敌人，自动推进
            if (!engine.isPlayerActing()) {
                delay(500)
                engine.advanceToNextActor()
                battleState = engine.getBattleState()
                onStateChange()
            }
        }
    }

    // 检查游戏结束
    LaunchedEffect(battleState.isGameOver) {
        if (battleState.isGameOver) {
            delay(1500)
            if (battleState.isVictory) onVictory() else onDefeat()
        }
    }

    val activeChar = battleState.playerTeam.find { it.id == battleState.activeCharacterId }
    val isPlayerTurn = battleState.isPlayerTurn && !battleState.isGameOver && !processing

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {

            // ── 顶栏：回合信息 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("第${battleState.currentTurn}回合", fontSize = 14.sp, color = TextSecondary)
                Text("⚔️ ${getStageName(stage)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                IconButton(onClick = { showLog = !showLog }) {
                    Icon(Icons.Default.List, "日志", tint = TextSecondary)
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── 敌方区域 ──
            SectionLabel("敌方", AccentRed)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                battleState.enemyTeam.forEach { enemy ->
                    BattleCharacterCard(
                        char = enemy,
                        isTargetable = isPlayerTurn && selectedSkill != null && enemy.isAlive &&
                            (selectedSkill?.targetType == TargetType.SINGLE_ENEMY),
                        isCurrentActor = battleState.activeCharacterId == enemy.id,
                        onTargetClick = {
                            selectedSkill?.let { skill ->
                                processing = true
                                engine.playerExecuteSkill(skill, enemy)
                                battleState = engine.getBattleState()
                                selectedSkill = null
                                showSkills = false
                                processing = false
                                onStateChange()
                                // 如果接下来是敌人回合，延迟推进
                                if (!battleState.isGameOver && !engine.isPlayerActing()) {
                                    processing = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── 战斗日志（折叠区域）──
            if (showLog) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        reverseLayout = true
                    ) {
                        items(battleState.battleLog.takeLast(60).reversed()) { entry ->
                            Text(
                                entry.text,
                                fontSize = 11.sp,
                                color = when (entry.type) {
                                    LogType.DAMAGE -> AccentRed
                                    LogType.HEAL -> AccentGreen
                                    LogType.SKILL -> AccentGold
                                    LogType.STATUS -> AccentCyan
                                    LogType.SYSTEM -> TextSecondary
                                    LogType.VICTORY -> AccentGreen
                                    LogType.DEFEAT -> AccentRed
                                    else -> TextPrimary
                                },
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            } else {
                // 最近3条日志
                Card(
                    modifier = Modifier.height(56.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(6.dp), verticalArrangement = Arrangement.Center) {
                        battleState.battleLog.takeLast(3).forEach { entry ->
                            Text(
                                entry.text,
                                fontSize = 10.sp,
                                color = when (entry.type) {
                                    LogType.DAMAGE -> AccentRed
                                    LogType.HEAL -> AccentGreen
                                    LogType.SKILL -> AccentGold
                                    LogType.STATUS -> AccentCyan
                                    LogType.SYSTEM -> TextSecondary
                                    else -> TextPrimary
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── 我方区域 ──
            SectionLabel("我方", AccentBlue)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                battleState.playerTeam.forEach { char ->
                    BattleCharacterCard(
                        char = char,
                        isTargetable = false,
                        isCurrentActor = battleState.activeCharacterId == char.id && char.isPlayer,
                        onTargetClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── 技能面板 ──
            if (isPlayerTurn && activeChar != null && activeChar.isAlive) {
                PlayerActionPanel(
                    character = activeChar,
                    showSkills = showSkills,
                    onToggleSkills = { showSkills = !showSkills },
                    onSkillSelected = { skill ->
                        selectedSkill = skill
                        showSkills = false
                        // 如果是自技能或全体技能，直接执行
                        if (skill.targetType == TargetType.SELF || skill.targetType == TargetType.ALL_ALLIES || skill.isAOE) {
                            processing = true
                            engine.playerExecuteSkill(skill, null)
                            battleState = engine.getBattleState()
                            selectedSkill = null
                            processing = false
                            onStateChange()
                        }
                    },
                    onSkip = {
                        processing = true
                        engine.playerSkipTurn()
                        battleState = engine.getBattleState()
                        processing = false
                        onStateChange()
                    },
                    canUseSkill = { skill -> engine.canUseSkill(activeChar, skill) }
                )
            } else if (!battleState.isGameOver && !isPlayerTurn) {
                // 敌人回合提示
                Box(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("敌方行动中...", fontSize = 16.sp, color = AccentRed)
                }
            }

            // ── 目标选择提示 ──
            if (selectedSkill != null && isPlayerTurn) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👉 选择目标 — ${selectedSkill!!.name}", fontSize = 14.sp, color = AccentGold)
                }
            }
        }

        // ── 敌人回合自动推进 ──
        LaunchedEffect(processing, battleState) {
            if (processing && !engine.isPlayerActing() && !battleState.isGameOver) {
                delay(800)
                engine.advanceToNextActor()
                battleState = engine.getBattleState()
                onStateChange()
                if (engine.isPlayerActing() || battleState.isGameOver) {
                    processing = false
                }
            }
        }
    }
}

@Composable
fun SectionLabel(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(4.dp).height(16.dp).background(color))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// ═══════════════════════════════════════════════════════════
//  角色卡片
// ═══════════════════════════════════════════════════════════

@Composable
fun BattleCharacterCard(
    char: GameCharacter,
    isTargetable: Boolean,
    isCurrentActor: Boolean,
    onTargetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatar = CharacterFactory.getAvatar(char)
    val color = Color(CharacterFactory.getColor(char))

    val cardModifier = modifier
        .clickable(enabled = isTargetable, onClick = onTargetClick)

    Card(
        modifier = cardModifier
            .then(
                if (isCurrentActor) Modifier.border(2.dp, AccentGold, RoundedCornerShape(8.dp))
                else if (isTargetable) Modifier.border(2.dp, AccentRed.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (!char.isAlive) Color(0xFF1a1a1a) else CardBg
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            // 头像+名字
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    avatar,
                    fontSize = 20.sp,
                    modifier = Modifier.alpha(if (char.isAlive) 1f else 0.3f)
                )
                Spacer(Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (char.isPlayer) "${char.title}·${char.name}" else char.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (char.isAlive) color else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!char.isAlive) {
                        Text("已倒下", fontSize = 9.sp, color = AccentRed)
                    }
                }
            }

            if (char.isAlive) {
                // HP条
                Spacer(Modifier.height(4.dp))
                HpBar(hp = char.hp, maxHp = char.maxHp, shield = char.shield)

                // 属性条
                Spacer(Modifier.height(3.dp))

                // AP
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚪", fontSize = 10.sp)
                    Text("${char.ap}/${char.maxAp}", fontSize = 9.sp, color = ApGold)
                    Spacer(Modifier.weight(1f))
                    // 势/意/悲岩
                    if (char.momentum > 0) {
                        Text("势${char.momentum}", fontSize = 9.sp, color = AccentOrange)
                        Spacer(Modifier.width(4.dp))
                    }
                    if (char.momentumPressure > 0) {
                        Text("压${char.momentumPressure}", fontSize = 9.sp, color = AccentRed)
                        Spacer(Modifier.width(4.dp))
                    }
                    if (char.intent > 0) {
                        Text("意${char.intent}", fontSize = 9.sp, color = AccentPurple)
                        Spacer(Modifier.width(4.dp))
                    }
                    if (char.sorrowRock > 0 && char.id == "yan") {
                        Text("悲${char.sorrowRock}", fontSize = 9.sp, color = Color(0xFF8D6E63))
                    }
                }

                // 状态图标
                if (char.statuses.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Row {
                        char.statuses.take(4).forEach { s ->
                            val icon = when (s.id) {
                                "sea_tide" -> "🌊"
                                "crit_up" -> "🎯"
                                "crit_dmg_up" -> "💥"
                                "enchant" -> "✨"
                                "counter" -> "🛡️"
                                "defense_stance" -> "🧱"
                                else -> "📌"
                            }
                            Text("$icon${if (s.duration > 0) s.duration else ""}", fontSize = 10.sp)
                            Spacer(Modifier.width(2.dp))
                        }
                    }
                }

                // 极意标记
                if (char.hasExtremeIntent) {
                    Text("★极意", fontSize = 9.sp, color = AccentGold, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HpBar(hp: Int, maxHp: Int, shield: Int) {
    val hpPercent = if (maxHp > 0) hp.toFloat() / maxHp else 0f
    val hpColor = when {
        hpPercent > 0.6f -> HpGreen
        hpPercent > 0.3f -> HpYellow
        else -> HpRed
    }

    Column {
        // 护盾条
        if (shield > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(CardBgLight, RoundedCornerShape(2.dp))
            ) {
                val shieldPercent = minOf(shield.toFloat() / maxHp, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(shieldPercent)
                        .height(4.dp)
                        .background(ShieldBlue, RoundedCornerShape(2.dp))
                )
            }
            Spacer(Modifier.height(1.dp))
        }
        // HP条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(CardBgLight, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(hpPercent)
                    .height(6.dp)
                    .background(hpColor, RoundedCornerShape(3.dp))
            )
        }
        // 数值
        Text("$hp/$maxHp${if (shield > 0) " 🛡$shield" else ""}", fontSize = 9.sp, color = TextSecondary)
    }
}

// ═══════════════════════════════════════════════════════════
//  玩家操作面板
// ═══════════════════════════════════════════════════════════

@Composable
fun PlayerActionPanel(
    character: GameCharacter,
    showSkills: Boolean,
    onToggleSkills: () -> Unit,
    onSkillSelected: (SkillDef) -> Unit,
    onSkip: () -> Unit,
    canUseSkill: (SkillDef) -> Boolean
) {
    Column {
        // 当前角色信息
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg, RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(CharacterFactory.getAvatar(character), fontSize = 24.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text("${character.title}·${character.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(CharacterFactory.getColor(character)))
                Row {
                    Text("ATK ${character.effectiveAtk}", fontSize = 10.sp, color = TextSecondary)
                    Spacer(Modifier.width(12.dp))
                    Text("暴击 ${character.effectiveCritRate}%", fontSize = 10.sp, color = TextSecondary)
                    Spacer(Modifier.width(12.dp))
                    Text("⚪ ${character.ap}/${character.maxAp}", fontSize = 10.sp, color = ApGold)
                }
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onSkip) {
                Text("跳过", fontSize = 12.sp, color = TextSecondary)
            }
        }

        Spacer(Modifier.height(6.dp))

        if (showSkills) {
            // 技能列表
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(character.skills) { skill ->
                    SkillCard(
                        skill = skill,
                        enabled = canUseSkill(skill),
                        onClick = { onSkillSelected(skill) }
                    )
                }
            }
        } else {
            // 显示技能按钮
            Button(
                onClick = onToggleSkills,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardBgLight),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("选择技能 (${character.skills.size})", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SkillCard(skill: SkillDef, enabled: Boolean, onClick: () -> Unit) {
    val apColor = if (skill.apCost > 0) AccentRed else if (skill.apCost < 0) AccentGreen else TextSecondary
    val typeColor = when (skill.type) {
        SkillType.BASIC -> AccentBlue
        SkillType.SKILL_1 -> AccentCyan
        SkillType.SKILL_2 -> AccentPurple
        SkillType.SKILL_3 -> AccentGold
        SkillType.DERIVED -> AccentOrange
        else -> TextSecondary
    }

    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) CardBgLight else Color(0xFF111111)
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, typeColor.copy(alpha = if (enabled) 0.6f else 0.2f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(skill.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (enabled) typeColor else TextSecondary)
                Spacer(Modifier.weight(1f))
                Text(
                    if (skill.apCost > 0) "消耗${skill.apCost}⚪" else if (skill.apCost < 0) "回复${-skill.apCost}⚪" else "无消耗",
                    fontSize = 9.sp, color = apColor
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(skill.description, fontSize = 10.sp, color = TextSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
            if (skill.requiresMomentum > 0 || skill.requiresIntent > 0) {
                val req = mutableListOf<String>()
                if (skill.requiresMomentum > 0) req.add("需要势≥${skill.requiresMomentum}")
                if (skill.requiresIntent > 0) req.add("需要意≥${skill.requiresIntent}")
                Text(req.joinToString(" "), fontSize = 9.sp, color = AccentOrange)
            }
            if (skill.endsTurn) {
                Text("结束后回合结束", fontSize = 9.sp, color = TextSecondary)
            } else {
                Text("不结束回合", fontSize = 9.sp, color = AccentGreen)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  关卡通关 / 结果界面
// ═══════════════════════════════════════════════════════════

@Composable
fun StageClearScreen(stage: Int, onNext: () -> Unit, onMenu: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1A2E1A), DarkBg))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏆", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text("第${stage}关 通关！", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
            Spacer(Modifier.height(8.dp))
            Text("队伍恢复25%生命值", fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.height(32.dp))
            if (stage < 3) {
                Button(
                    onClick = onNext,
                    modifier = Modifier.width(200.dp).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("进入第${stage + 1}关", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text("🎉 全部通关！恭喜！", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AccentGold)
            }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onMenu) { Text("返回主菜单", color = TextSecondary) }
        }
    }
}

@Composable
fun ResultScreen(isVictory: Boolean, onMenu: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                if (isVictory) listOf(Color(0xFF1A2E1A), DarkBg)
                else listOf(Color(0xFF2E1A1A), DarkBg)
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (isVictory) "🎉" else "💀", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                if (isVictory) "胜利！" else "失败...",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = if (isVictory) AccentGreen else AccentRed
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onMenu,
                modifier = Modifier.width(200.dp).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isVictory) AccentGreen else AccentRed),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("返回主菜单", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  辅助函数
// ═══════════════════════════════════════════════════════════

private fun getStageName(stage: Int): String = when (stage) {
    1 -> "哥布林营地"
    2 -> "兽人要塞"
    3 -> "深渊王座"
    else -> "未知战场"
}
