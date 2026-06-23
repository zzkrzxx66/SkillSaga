package com.skillsaga.app

// ═══════════════════════════════════════════════════════════
//  数据模型层
// ═══════════════════════════════════════════════════════════

/** 角色定位 */
enum class Role { DPS, ASSASSIN, TANK }

/** 目标类型 */
enum class TargetType { SINGLE_ENEMY, ALL_ENEMIES, SELF, ALL_ALLIES }

/** 技能类型 */
enum class SkillType { BASIC, SKILL_1, SKILL_2, SKILL_3, DERIVED, PASSIVE, AUTO }

/** 状态效果 */
data class StatusEffect(
    val id: String,
    val name: String,
    var stacks: Int,
    var duration: Int,        // -1 = 永久, >0 = 回合数
    val description: String,
    val isBuff: Boolean = true,
    val isDebuff: Boolean = false
)

/** 伤害类型 */
enum class DamageType { NORMAL, FIXED }

/** 技能定义 */
data class SkillDef(
    val id: String,
    val name: String,
    val type: SkillType,
    val description: String,
    val targetType: TargetType,
    val apCost: Int,              // 正数=消耗, 负数=回复
    val endsTurn: Boolean,
    val maxUsesPerTurn: Int = 99,
    val requiresMomentum: Int = 0,  // 需要势的层数
    val requiresIntent: Int = 0,    // 需要意的层数
    val baseCritBonus: Int = 0,     // 使用时暴击加成
    val baseCritDmgBonus: Int = 0,  // 使用时暴伤加成
    val isAOE: Boolean = false
)

/** 角色实例 */
data class GameCharacter(
    val id: String,
    val name: String,
    val title: String,
    val role: Role,
    var hp: Int,
    val maxHp: Int,
    val atk: Int,
    val speed: Int,
    var ap: Int,
    val maxAp: Int,
    var shield: Int,
    var momentum: Int,       // 势
    var momentumPressure: Int, // 势压
    var intent: Int,         // 意
    var extremeIntent: Int,  // 极意
    var sorrowRock: Int,     // 悲岩
    var baseCritRate: Int = 5,    // 基础暴击率 %
    var baseCritDmg: Int = 150,   // 基础暴击伤害 %
    var critRateBonus: Int = 0,   // 临时暴击率加成
    var critDmgBonus: Int = 0,    // 临时暴击伤害加成
    var armorPenetration: Int = 0, // 穿甲 %
    var extraDamage: Int = 0,     // 额外固定伤害
    var bonusAtk: Int = 0,        // 临时攻击力加成
    val statuses: MutableList<StatusEffect> = mutableListOf(),
    val skills: List<SkillDef> = emptyList(),
    var isPlayer: Boolean = true,
    var hasActed: Boolean = false,
    var skillUseCount: MutableMap<String, Int> = mutableMapOf(),
    var actionCount: Int = 0,    // 本回合行动次数
    var enemyAtk: Int = 0        // 敌人的基础攻击力(用于AI)
) {
    /** 当前攻击力 = 基础 + 势加成(上限1倍) + 临时加成 */
    val effectiveAtk: Int
        get() {
            val momentumBonus = minOf(momentum, atk) // 势加成不超过原1倍
            return atk + momentumBonus + bonusAtk
        }

    /** 当前暴击率 */
    val effectiveCritRate: Int
        get() = baseCritRate + critRateBonus

    /** 当前暴击伤害 */
    val effectiveCritDmg: Int
        get() = baseCritDmg + critDmgBonus

    /** 是否存活 */
    val isAlive: Boolean get() = hp > 0

    /** 势加成是否已满 */
    val isMomentumCapped: Boolean get() = momentum >= atk

    /** 势压穿甲 */
    val pressureArmorPen: Int get() = momentumPressure * 10

    /** 势压额外伤害 */
    val pressureExtraDmg: Int get() = momentumPressure * 5

    /** 是否有海潮状态 */
    val hasSeaTide: Boolean get() = statuses.any { it.id == "sea_tide" }

    /** 是否有极意 */
    val hasExtremeIntent: Boolean get() = extremeIntent > 0

    /** 是否有附魔 */
    val hasEnchant: Boolean get() = statuses.any { it.id == "enchant" && it.stacks > 0 }

    /** 是否有反击 */
    val hasCounter: Boolean get() = statuses.any { it.id == "counter" && it.stacks > 0 }

    /** 是否有守势 */
    val hasDefenseStance: Boolean get() = statuses.any { it.id == "defense_stance" }

    /** 已损生命值 */
    val lostHp: Int get() = maxHp - hp

    /** 生命值百分比 */
    val hpPercent: Float get() = if (maxHp > 0) hp.toFloat() / maxHp else 0f
}

// ═══════════════════════════════════════════════════════════
//  战斗日志条目
// ═══════════════════════════════════════════════════════════

data class LogEntry(
    val text: String,
    val type: LogType = LogType.NORMAL,
    val turn: Int
)

enum class LogType { NORMAL, DAMAGE, HEAL, SKILL, STATUS, SYSTEM, VICTORY, DEFEAT }

// ═══════════════════════════════════════════════════════════
//  战斗状态快照
// ═══════════════════════════════════════════════════════════

data class BattleState(
    val playerTeam: List<GameCharacter>,
    val enemyTeam: List<GameCharacter>,
    val currentTurn: Int = 1,
    val isPlayerTurn: Boolean = true,
    val activeCharacterId: String? = null,
    val pendingSkill: SkillDef? = null,
    val awaitingTarget: Boolean = false,
    val isSelectingTarget: Boolean = false,
    val battleLog: List<LogEntry> = emptyList(),
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val turnOrder: List<String> = emptyList(),
    val currentActorIndex: Int = 0
)
