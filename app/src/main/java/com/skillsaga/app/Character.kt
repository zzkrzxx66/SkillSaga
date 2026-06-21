package com.skillsaga.app

data class Character(
    val id: String,
    val name: String,
    val title: String,
    var hp: Int,
    val maxHp: Int,
    val atk: Int,
    val speed: Int,
    var ap: Int, // Action Points
    val maxAp: Int,
    var shield: Int,
    var momentum: Int, // 势
    var intent: Int, // 意
    var sorrowRock: Int, // 悲岩
    val statuses: MutableList<Status> = mutableListOf(),
    val skills: List<Skill> = emptyList(),
    val isPlayer: Boolean = true
)

data class Status(
    val id: String,
    val name: String,
    var stacks: Int,
    var duration: Int? = null, // null = permanent
    val description: String = ""
)

data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val apCost: Int, // positive = cost, negative = gain
    val endsTurn: Boolean,
    val maxUsesPerTurn: Int? = null,
    val requiresCondition: String? = null, // e.g., "momentum>=10"
    val effect: (Character, Character, GameState) -> Unit
)

// Character definitions
object CharacterFactory {
    fun createDaHai(): Character {
        return Character(
            id = "dahai",
            name = "大海",
            title = "剑王",
            hp = 160,
            maxHp = 160,
            atk = 20,
            speed = 100,
            ap = 5, // Starting AP, adjust as needed
            maxAp = 10,
            shield = 0,
            momentum = 0,
            intent = 0,
            sorrowRock = 0,
            skills = listOf(
                Skill(
                    id = "dahai_skill1",
                    name = "斩潮式",
                    description = "对单体敌方造成1×1伤害；「势」+1",
                    apCost = 0,
                    endsTurn = true,
                    effect = { attacker, target, state ->
                        val damage = attacker.atk * 1
                        target.hp -= damage
                        attacker.momentum += 1
                        println("${attacker.name} 使用斩潮式，对 ${target.name} 造成 $damage 伤害，势+1")
                    }
                ),
                Skill(
                    id = "dahai_skill2",
                    name = "万江归海",
                    description = "「势」≥10时行动后自动发动；「势」-10。对全体敌方造成1×1伤害",
                    apCost = 0,
                    endsTurn = false,
                    effect = { attacker, target, state ->
                        // This is auto-triggered, not a player skill
                        if (attacker.momentum >= 10) {
                            attacker.momentum -= 10
                            val damage = attacker.atk * 1
                            // AOE damage to all enemies
                            state.enemies.forEach { enemy ->
                                enemy.hp -= damage
                            }
                            println("${attacker.name} 自动发动万江归海，对全体敌方造成 $damage 伤害，势-10")
                        }
                    }
                ),
                Skill(
                    id = "dahai_skill3",
                    name = "叠浪式",
                    description = "对单体敌方造成1×0.5伤害；每回合第x次使用时「势」+x（x≥3后固定+0）",
                    apCost = 1, // +1 AP (gain)
                    endsTurn = false,
                    effect = { attacker, target, state ->
                        val damage = (attacker.atk * 0.5).toInt()
                        target.hp -= damage
                        val usesThisTurn = state.getSkillUsesThisTurn(attacker.id, "dahai_skill3")
                        val momentumGain = if (usesThisTurn < 3) usesThisTurn else 0
                        attacker.momentum += momentumGain
                        println("${attacker.name} 使用叠浪式，对 ${target.name} 造成 $damage 伤害，势+$momentumGain")
                    }
                ),
                Skill(
                    id = "dahai_skill4",
                    name = "月海潮生",
                    description = "暴击率+20%，暴伤+50%。获得强「海潮」状态",
                    apCost = -5, // costs 5 AP
                    endsTurn = false,
                    effect = { attacker, target, state ->
                        // Add status effects
                        attacker.statuses.add(Status("crit_up", "暴击提升", 1, 2, "暴击率+20%"))
                        attacker.statuses.add(Status("crit_dmg_up", "暴伤提升", 1, 2, "暴伤+50%"))
                        attacker.statuses.add(Status("sea_tide", "海潮", 1, 3, "势不会减少"))
                        println("${attacker.name} 使用月海潮生，获得暴击提升和海潮状态")
                    }
                )
            )
        )
    }

    fun createWei(): Character {
        return Character(
            id = "wei",
            name = "薇",
            title = "隐月",
            hp = 130,
            maxHp = 130,
            atk = 25,
            speed = 110,
            ap = 5,
            maxAp = 10,
            shield = 0,
            momentum = 0,
            intent = 0,
            sorrowRock = 0,
            skills = listOf(
                Skill(
                    id = "wei_skill1",
                    name = "明霄剑气",
                    description = "获得1层「附魔」+1层「反击」",
                    apCost = -1, // costs 1 AP
                    endsTurn = false,
                    maxUsesPerTurn = 2,
                    effect = { attacker, target, state ->
                        attacker.statuses.add(Status("enchant", "附魔", 1, null, "攻击附加原伤害×0.5的额外伤害"))
                        attacker.statuses.add(Status("counter", "反击", 1, null, "受击时消耗1层进行反击"))
                        println("${attacker.name} 使用明霄剑气，获得附魔和反击")
                    }
                ),
                Skill(
                    id = "wei_skill2",
                    name = "幻云斩",
                    description = "对单体造成1×1伤害；「意」+1",
                    apCost = 1, // +1 AP (gain)
                    endsTurn = true,
                    effect = { attacker, target, state ->
                        val damage = attacker.atk * 1
                        target.hp -= damage
                        attacker.intent += 1
                        println("${attacker.name} 使用幻云斩，对 ${target.name} 造成 $damage 伤害，意+1")
                    }
                ),
                Skill(
                    id = "wei_skill3",
                    name = "明静心决",
                    description = "获得3层「意」；暴伤+50%（持续2回合）",
                    apCost = -2, // costs 2 AP
                    endsTurn = false,
                    maxUsesPerTurn = 1,
                    effect = { attacker, target, state ->
                        attacker.intent += 3
                        attacker.statuses.add(Status("crit_dmg_up", "暴伤提升", 1, 2, "暴伤+50%"))
                        println("${attacker.name} 使用明静心决，意+3，获得暴伤提升")
                    }
                ),
                Skill(
                    id = "wei_skill4",
                    name = "天威明剑决",
                    description = "对全体敌方造成1×0.8伤害×2次；对随机目标造成「意」层数×1的固有伤害",
                    apCost = -3, // costs 3 AP
                    endsTurn = true,
                    requiresCondition = "intent>=3",
                    effect = { attacker, target, state ->
                        val damage = (attacker.atk * 0.8).toInt()
                        // AOE damage twice
                        state.enemies.forEach { enemy ->
                            enemy.hp -= damage * 2
                        }
                        // Random target additional damage
                        val randomEnemy = state.enemies.random()
                        val additionalDamage = attacker.intent * 1
                        randomEnemy.hp -= additionalDamage
                        println("${attacker.name} 使用天威明剑决，对全体造成 ${damage * 2} 伤害，对 ${randomEnemy.name} 额外造成 $additionalDamage 固有伤害")
                    }
                )
            )
        )
    }

    fun createYan(): Character {
        return Character(
            id = "yan",
            name = "岩",
            title = "破军",
            hp = 180,
            maxHp = 180,
            atk = 10,
            speed = 85,
            ap = 5,
            maxAp = 10,
            shield = 0,
            momentum = 0,
            intent = 0,
            sorrowRock = 0,
            skills = listOf(
                Skill(
                    id = "yan_skill1",
                    name = "镇山岳",
                    description = "对单体造成1×0.8伤害；「势」+2；获得10+「悲岩」层数的护盾",
                    apCost = 1, // +1 AP (gain)
                    endsTurn = true,
                    effect = { attacker, target, state ->
                        val damage = (attacker.atk * 0.8).toInt()
                        target.hp -= damage
                        attacker.momentum += 2
                        val shieldGain = 10 + attacker.sorrowRock
                        attacker.shield += shieldGain
                        println("${attacker.name} 使用镇山岳，对 ${target.name} 造成 $damage 伤害，势+2，获得 $shieldGain 护盾")
                    }
                ),
                Skill(
                    id = "yan_skill2",
                    name = "峰峦起",
                    description = "获得当前「势」×0.5的「势」；为全体队友添加「悲岩」层数的护盾",
                    apCost = -1, // costs 1 AP
                    endsTurn = false,
                    effect = { attacker, target, state ->
                        val momentumGain = (attacker.momentum * 0.5).toInt()
                        attacker.momentum += momentumGain
                        // Add shield to all allies based on sorrowRock
                        state.allies.forEach { ally ->
                            ally.shield += attacker.sorrowRock
                        }
                        println("${attacker.name} 使用峰峦起，势+$momentumGain，为全体队友添加 ${attacker.sorrowRock} 护盾")
                    }
                ),
                Skill(
                    id = "yan_skill3",
                    name = "层峦叠嶂",
                    description = "对单体造成1×1伤害；目标「势」-5",
                    apCost = -1, // costs 1 AP
                    endsTurn = false,
                    effect = { attacker, target, state ->
                        val damage = attacker.atk * 1
                        target.hp -= damage
                        target.momentum -= 5
                        if (target.momentum < 0) target.momentum = 0
                        println("${attacker.name} 使用层峦叠嶂，对 ${target.name} 造成 $damage 伤害，目标势-5")
                    }
                ),
                Skill(
                    id = "yan_skill4",
                    name = "拜岳撼天",
                    description = "消耗全部「势」，对全体造成1×0.5伤害+每消耗1层势额外+2伤害+当前护盾值等额伤害",
                    apCost = -3, // costs 3 AP
                    endsTurn = true,
                    effect = { attacker, target, state ->
                        val momentumConsumed = attacker.momentum
                        attacker.momentum = 0
                        val baseDamage = (attacker.atk * 0.5).toInt()
                        val bonusDamage = momentumConsumed * 2
                        val shieldDamage = attacker.shield
                        val totalDamage = baseDamage + bonusDamage + shieldDamage
                        // AOE damage
                        state.enemies.forEach { enemy ->
                            enemy.hp -= totalDamage
                        }
                        attacker.shield = 0
                        println("${attacker.name} 使用拜岳撼天，消耗 $momentumConsumed 势，对全体造成 $totalDamage 伤害")
                    }
                )
            )
        )
    }
}

// Game state to track skill uses and other info
data class GameState(
    val allies: List<Character>,
    val enemies: List<Character>,
    val turnNumber: Int = 1,
    private val skillUses: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()
) {
    fun getSkillUsesThisTurn(characterId: String, skillId: String): Int {
        return skillUses[characterId]?.get(skillId) ?: 0
    }

    fun recordSkillUse(characterId: String, skillId: String) {
        val charMap = skillUses.getOrPut(characterId) { mutableMapOf() }
        charMap[skillId] = (charMap[skillId] ?: 0) + 1
    }

    fun resetTurn() {
        skillUses.clear()
    }
}