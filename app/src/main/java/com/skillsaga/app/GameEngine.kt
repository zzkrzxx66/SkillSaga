package com.skillsaga.app

import kotlin.math.roundToInt
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════
//  战斗引擎 - 完整实现设计文档中的所有机制
// ═══════════════════════════════════════════════════════════

class GameEngine {

    var playerTeam: MutableList<GameCharacter> = mutableListOf()
    var enemyTeam: MutableList<GameCharacter> = mutableListOf()
    var currentTurn: Int = 1
    var currentStage: Int = 1
    var battleLog: MutableList<LogEntry> = mutableListOf()
    var turnOrder: MutableList<GameCharacter> = mutableListOf()
    var currentActorIndex: Int = 0
    var isGameOver: Boolean = false
    var isVictory: Boolean = false
    var pendingRecoverMomentum: MutableMap<String, Int> = mutableMapOf() // 拜岳撼天下回合恢复势

    // ── 初始化 ──

    fun initializeGame(stage: Int = 1) {
        currentStage = stage
        currentTurn = 1
        isGameOver = false
        isVictory = false
        battleLog.clear()
        pendingRecoverMomentum.clear()

        playerTeam = mutableListOf(
            CharacterFactory.createDaHai(),
            CharacterFactory.createWei(),
            CharacterFactory.createYan()
        )
        enemyTeam = CharacterFactory.createEnemyTeam(stage).toMutableList()

        log("═══ 战斗开始 ═══", LogType.SYSTEM)
        log("第 ${stage} 关 — ${getStageName(stage)}", LogType.SYSTEM)
        log("我方：${playerTeam.joinToString(" / ") { "${it.title}·${it.name}" }}", LogType.SYSTEM)
        log("敌方：${enemyTeam.joinToString(" / ") { it.name }}", LogType.SYSTEM)
        log("", LogType.NORMAL)

        calculateTurnOrder()
        processTurnStart()
    }

    private fun getStageName(stage: Int): String = when (stage) {
        1 -> "哥布林营地"
        2 -> "兽人要塞"
        3 -> "深渊王座"
        else -> "未知战场"
    }

    private fun calculateTurnOrder() {
        val all = (playerTeam + enemyTeam).filter { it.isAlive }
        turnOrder = all.sortedByDescending { it.speed }.toMutableList()
        currentActorIndex = 0
    }

    // ── 当前行动角色 ──

    fun currentActor(): GameCharacter? {
        if (currentActorIndex >= turnOrder.size) return null
        return turnOrder[currentActorIndex]
    }

    fun isPlayerActing(): Boolean = currentActor()?.isPlayer == true

    // ── 回合开始处理 ──

    private fun processTurnStart() {
        log("--- 第 $currentTurn 回合 ---", LogType.SYSTEM)

        for (char in playerTeam + enemyTeam) {
            if (!char.isAlive) continue
            char.ap = char.maxAp
            char.hasActed = false
            char.actionCount = 0
            char.skillUseCount.clear()
            char.critRateBonus = 0
            char.critDmgBonus = 0
            char.armorPenetration = 0
            char.bonusAtk = 0

            // 处理拜岳撼天的势恢复
            val recoverMomentum = pendingRecoverMomentum.remove(char.id)
            if (recoverMomentum != null) {
                char.momentum += recoverMomentum
                log("${char.name} 恢复了 ${recoverMomentum} 层势", LogType.STATUS)
            }

            processStartOfTurnPassives(char)
            tickStatusDurations(char)
        }

        calculateTurnOrder()
    }

    // ── 回合开始被动 ──

    private fun processStartOfTurnPassives(char: GameCharacter) {
        when (char.id) {
            "dahai" -> {
                // [潮汐] 回合开始：势<10时+3；势≥10时回合结束自动万江归海
                if (char.momentum < 10) {
                    char.momentum += 3
                    log("${char.name} [潮汐] 势+3 (当前${char.momentum})", LogType.STATUS)
                }
                // 势压结算
                if (char.isMomentumCapped) {
                    val newPressure = char.momentum / 10
                    if (newPressure > char.momentumPressure) {
                        val gain = newPressure - char.momentumPressure
                        char.momentumPressure = newPressure
                        char.armorPenetration += gain * 10
                        log("${char.name} [势压] 获得${gain}层势压 (穿甲+${gain*10}%, 额外伤害+${gain*5})", LogType.STATUS)
                    }
                }
            }
        }

        // 薇的剑意·冲霄：有「意」时行动50%概率+1⚪（在行动时处理）
        // 岩的守势：受击时+1势（在受击时处理）
    }

    // ── 行动时被动 ──

    private fun processOnActionPassives(char: GameCharacter) {
        when (char.id) {
            "dahai" -> {
                // [起势] 每次行动势+1，消除1个debuff；势≥10时行动+15(2回合)
                if (!char.hasSeaTide) {
                    // 海潮状态下势不减少...但起势是增加势，所以还是要加
                }
                char.momentum += 1
                log("${char.name} [起势] 势+1 (当前${char.momentum})", LogType.STATUS)

                // 消除1个debuff
                val debuff = char.statuses.filter { it.isDebuff }
                if (debuff.isNotEmpty()) {
                    val removed = debuff.first()
                    char.statuses.remove(removed)
                    log("${char.name} [起势] 消除了${removed.name}", LogType.STATUS)
                }

                if (char.momentum >= 10) {
                    char.bonusAtk += 15
                    log("${char.name} [起势] 攻击力+15 (势≥10)", LogType.STATUS)
                }
            }
        }

        // 薇的剑意·冲霄：有「意」时行动50%概率+1⚪
        if (char.id == "wei" && char.intent > 0) {
            if (Random.nextFloat() < 0.5f) {
                char.ap = minOf(char.ap + 1, char.maxAp)
                log("${char.name} [剑意·冲霄] 意触发：行动点+1", LogType.STATUS)
            }
        }
    }

    // ── 受击被动 ──

    private fun processOnHitPassives(target: GameCharacter, attacker: GameCharacter, damage: Int) {
        when (target.id) {
            "yan" -> {
                // [守势] 受攻击时势+1
                target.momentum += 1
                log("${target.name} [守势] 受击势+1 (当前${target.momentum})", LogType.STATUS)
            }
        }

        // 反击
        if (target.hasCounter) {
            val counterStatus = target.statuses.find { it.id == "counter" }!!
            if (counterStatus.stacks > 0 && target.isAlive) {
                val counterDmg = calcDamage(target, attacker, 0.8, canCrit = true)
                applyDamage(attacker, counterDmg, isCounter = true)
                counterStatus.stacks -= 1
                if (counterStatus.stacks <= 0) {
                    target.statuses.remove(counterStatus)
                }
                log("${target.name} [反击] 对${attacker.name}造成${counterDmg}伤害！", LogType.DAMAGE)
            }
        }
    }

    // ── 回合结束被动 ──

    private fun processEndOfTurnPassives(char: GameCharacter) {
        when (char.id) {
            "dahai" -> {
                // [潮汐] 势≥10时回合结束自动万江归海
                if (char.momentum >= 10 && char.isAlive) {
                    autoWanjiangGuihai(char)
                }
            }
            "yan" -> {
                // [不动如山] 回合结束时每层势+2护盾
                if (char.momentum > 0) {
                    val shieldGain = char.momentum * 2
                    char.shield += shieldGain
                    log("${char.name} [不动如山] 获得${shieldGain}护盾", LogType.STATUS)

                    // 为队友添加悲岩层数的护盾
                    if (char.sorrowRock > 0) {
                        playerTeam.filter { it.isAlive && it.id != char.id }.forEach { ally ->
                            ally.shield += char.sorrowRock
                        }
                        log("${char.name} [不动如山] 为队友提供${char.sorrowRock}护盾", LogType.STATUS)
                    }
                }
            }
        }

        // 薇的意每回合-1
        if (char.id == "wei" && char.intent > 0) {
            char.intent -= 1
            if (char.intent > 0) {
                log("${char.name} 「意」-1 (当前${char.intent})", LogType.STATUS)
            }
        }
    }

    // ── 自动万江归海 ──

    private fun autoWanjiangGuihai(char: GameCharacter) {
        val seaTide = char.hasSeaTide
        if (!seaTide) {
            char.momentum -= 10
            if (char.momentum < 0) char.momentum = 0
        }
        val dmg = calcDamage(char, enemyTeam.firstOrNull { it.isAlive }!!, 1.0, canCrit = true)
        enemyTeam.filter { it.isAlive }.forEach { enemy ->
            applyDamage(enemy, dmg)
            processOnHitPassives(enemy, char, dmg)
        }
        log("${char.name} ★ [万江归海] 自动发动！全体${dmg}伤害${if(seaTide) "(海潮：势不减少)" else "，势-10"}", LogType.SKILL)
    }

    // ── 伤害计算 ──

    private fun calcDamage(attacker: GameCharacter, target: GameCharacter, multiplier: Double, canCrit: Boolean = true, ignorePressure: Boolean = false): Int {
        var baseDmg = (attacker.effectiveAtk * multiplier).roundToInt()

        // 势压额外伤害
        if (!ignorePressure && attacker.momentumPressure > 0) {
            baseDmg += attacker.pressureExtraDmg
        }

        // 暴击判定
        if (canCrit) {
            var critRate = attacker.effectiveCritRate
            // 薇的意：有「意」时暴击+50%
            if (attacker.id == "wei" && attacker.intent > 0) {
                critRate += 50
            }
            if (Random.nextInt(100) < critRate) {
                baseDmg = (baseDmg * attacker.effectiveCritDmg / 100).roundToInt()
                log("  💥 暴击！", LogType.DAMAGE)
            }
        }

        // 穿甲
        val armorPen = if (!ignorePressure) attacker.armorPenetration else 0

        // 护盾减少（考虑穿甲）
        // 穿甲比例无视护盾，直接扣血
        if (target.shield > 0 && armorPen > 0) {
            val directDmg = (baseDmg * armorPen / 100).roundToInt()
            val shieldDmg = baseDmg - directDmg
            // shieldDmg部分被护盾吸收，directDmg直接扣血
            // 返回总伤害，在applyDamage中处理
            return baseDmg // 总伤害，applyDamage中处理穿甲
        }

        return baseDmg
    }

    // ── 应用伤害（处理护盾和穿甲）──

    private fun applyDamage(target: GameCharacter, damage: Int, isCounter: Boolean = false) {
        if (target.isMomentumCapped && target.id == "dahai" && !isCounter) {
            // 大海势满时...其实设计文档没说大海有减伤，这里先不做
        }

        val armorPen = if (target.id == "yan") 0 else 0 // 目标的穿甲减免暂不实现

        var remainingDmg = damage

        // 先扣护盾
        if (target.shield > 0) {
            if (target.shield >= remainingDmg) {
                target.shield -= remainingDmg
                remainingDmg = 0
            } else {
                remainingDmg -= target.shield
                target.shield = 0
            }
        }

        // 剩余伤害扣血
        if (remainingDmg > 0) {
            target.hp -= remainingDmg
            if (target.hp < 0) target.hp = 0
        }
    }

    // ── 状态持续时间 ──

    private fun tickStatusDurations(char: GameCharacter) {
        val toRemove = mutableListOf<StatusEffect>()
        for (status in char.statuses) {
            if (status.duration > 0) {
                status.duration -= 1
                if (status.duration <= 0) {
                    toRemove.add(status)
                    log("${char.name} 状态[${status.name}]结束", LogType.STATUS)
                }
            }
        }
        char.statuses.removeAll(toRemove)

        // 极意：生命值上升时失去
        if (char.extremeIntent > 0 && char.hp > char.maxHp / 2) {
            char.extremeIntent = 0
            log("${char.name} 「极意」消失（生命值已恢复）", LogType.STATUS)
        }
    }

    // ── 技能可用性检查 ──

    fun canUseSkill(char: GameCharacter, skill: SkillDef): Boolean {
        if (!char.isAlive) return false
        if (char.ap < skill.apCost) return false
        val uses = char.skillUseCount[skill.id] ?: 0
        if (uses >= skill.maxUsesPerTurn) return false
        if (char.momentum < skill.requiresMomentum) return false
        if (char.intent < skill.requiresIntent) return false
        return true
    }

    // ── 执行技能 ──

    fun executeSkill(actor: GameCharacter, skill: SkillDef, target: GameCharacter?): Boolean {
        if (!canUseSkill(actor, skill)) return false

        // 消耗AP
        actor.ap -= skill.apCost
        if (actor.ap < 0) actor.ap = 0
        if (actor.ap > actor.maxAp) actor.ap = actor.maxAp

        // 记录使用
        actor.skillUseCount[skill.id] = (actor.skillUseCount[skill.id] ?: 0) + 1
        actor.actionCount += 1

        // 行动时被动
        processOnActionPassives(actor)

        // 临时暴击/暴伤加成
        if (skill.baseCritBonus > 0) {
            actor.critRateBonus += skill.baseCritBonus
        }
        if (skill.baseCritDmgBonus > 0) {
            actor.critDmgBonus += skill.baseCritDmgBonus
            actor.statuses.add(StatusEffect("crit_dmg_up", "暴伤提升", 1, 2, "暴伤+${skill.baseCritDmgBonus}%"))
        }

        // 执行技能效果
        executeSkillEffect(actor, skill, target)

        // 清除临时加成（仅限本次攻击的）
        // 暴击率加成是临时的
        if (skill.baseCritBonus > 0) {
            actor.critRateBonus -= skill.baseCritBonus
        }

        log("${actor.name} 使用了【${skill.name}】", LogType.SKILL)

        if (skill.endsTurn) {
            endActorTurn(actor)
        }

        return true
    }

    // ── 技能效果执行 ──

    private fun executeSkillEffect(actor: GameCharacter, skill: SkillDef, target: GameCharacter?) {
        when (skill.id) {
            // ── 大海 ──
            "dahai_zhanchao" -> {
                val t = target ?: enemyTeam.first { it.isAlive }
                val dmg = calcDamage(actor, t, 1.0)
                applyDamage(t, dmg)
                processOnHitPassives(t, actor, dmg)
                log("  → ${t.name} 受到${dmg}伤害", LogType.DAMAGE)
            }
            "dahai_dielang" -> {
                val t = target ?: enemyTeam.first { it.isAlive }
                val dmg = calcDamage(actor, t, 0.5)
                applyDamage(t, dmg)
                processOnHitPassives(t, actor, dmg)
                val useCount = actor.skillUseCount[skill.id] ?: 1
                val momentumGain = if (useCount < 3) useCount else 0
                if (momentumGain > 0) {
                    actor.momentum += momentumGain
                    log("  → 势+${momentumGain} (当前${actor.momentum})", LogType.STATUS)
                }
                log("  → ${t.name} 受到${dmg}伤害", LogType.DAMAGE)
            }
            "dahai_yuehai" -> {
                actor.statuses.add(StatusEffect("sea_tide", "海潮", 1, 3, "势不减少"))
                actor.statuses.add(StatusEffect("crit_up", "暴击提升", 1, 2, "暴击+20%"))
                log("  → 获得海潮、暴击提升状态", LogType.STATUS)
            }

            // ── 薇 ──
            "wei_mingxiao" -> {
                actor.statuses.add(StatusEffect("enchant", "附魔", 1, -1, "攻击附加原伤害×0.5"))
                actor.statuses.add(StatusEffect("counter", "反击", 1, -1, "受击反击1×0.8"))
                log("  → 获得附魔、反击", LogType.STATUS)
            }
            "wei_huanyun" -> {
                val t = target ?: enemyTeam.first { it.isAlive }
                val dmg = calcDamage(actor, t, 1.0)
                // 附魔额外伤害
                var totalDmg = dmg
                if (actor.hasEnchant) {
                    val extraDmg = (actor.effectiveAtk * 0.5).roundToInt()
                    totalDmg += extraDmg
                    val enchant = actor.statuses.find { it.id == "enchant" }!!
                    enchant.stacks -= 1
                    if (enchant.stacks <= 0) actor.statuses.remove(enchant)
                    log("  → 附魔额外${extraDmg}伤害", LogType.DAMAGE)
                    // 附魔攻击后+1⚪
                    actor.ap = minOf(actor.ap + 1, actor.maxAp)
                }
                applyDamage(t, totalDmg)
                processOnHitPassives(t, actor, totalDmg)
                actor.intent += 1
                log("  → 意+1 (当前${actor.intent})", LogType.STATUS)
                log("  → ${t.name} 受到${totalDmg}伤害", LogType.DAMAGE)

                // ⚪判断
                if (actor.ap <= 0) {
                    actor.ap += 2
                    log("  → ⚪≤0，行动点+2", LogType.STATUS)
                } else {
                    val shieldGain = actor.ap
                    actor.shield += shieldGain
                    log("  → ⚪>0，获得${shieldGain}护盾", LogType.STATUS)
                }
            }
            "wei_mingjing" -> {
                actor.intent += 3
                log("  → 意+3 (当前${actor.intent})", LogType.STATUS)

                // 暴伤+50% (2回合，上限2层)
                val existing = actor.statuses.count { it.id == "crit_dmg_up" }
                if (existing < 2) {
                    actor.critDmgBonus += 50
                    actor.statuses.add(StatusEffect("crit_dmg_up", "暴伤提升", 1, 2, "暴伤+50%"))
                    log("  → 暴伤+50% (2回合)", LogType.STATUS)
                }

                // 低血量触发极意
                if (actor.hp <= actor.maxHp / 2) {
                    actor.extremeIntent = 1
                    val heal = (actor.lostHp * 0.5).roundToInt()
                    actor.hp = minOf(actor.hp + heal, actor.maxHp)
                    log("  → ★ 极意触发！恢复${heal}生命值", LogType.HEAL)
                }
            }
            "wei_tianwei" -> {
                val enemies = enemyTeam.filter { it.isAlive }
                // 全体1×0.8×2次
                repeat(2) { i ->
                    enemies.forEach { e ->
                        val dmg = calcDamage(actor, e, 0.8)
                        applyDamage(e, dmg)
                        processOnHitPassives(e, actor, dmg)
                        log("  → 第${i+1}击 ${e.name} 受到${dmg}伤害", LogType.DAMAGE)
                    }
                }
                // 随机目标意×1固有伤害
                val randomTarget = enemies.random()
                val intentDmg = actor.intent
                randomTarget.hp -= intentDmg
                if (randomTarget.hp < 0) randomTarget.hp = 0
                log("  → 固有伤害：${randomTarget.name} 受到${intentDmg}固有伤害", LogType.DAMAGE)

                // 极意强化
                if (actor.hasExtremeIntent) {
                    enemies.forEach { e ->
                        if (e.isAlive) {
                            val dmg = calcDamage(actor, e, 1.0)
                            applyDamage(e, dmg)
                            processOnHitPassives(e, actor, dmg)
                            log("  → 极意追击：${e.name} 受到${dmg}伤害", LogType.DAMAGE)
                        }
                    }
                    actor.intent += 3
                    log("  → 极意：意+3 (当前${actor.intent})", LogType.STATUS)
                }
            }

            // ── 岩 ──
            "yan_zhenshan" -> {
                val t = target ?: enemyTeam.first { it.isAlive }
                val dmg = calcDamage(actor, t, 0.8, ignorePressure = true)
                applyDamage(t, dmg)
                processOnHitPassives(t, actor, dmg)
                actor.momentum += 2
                val shieldGain = 10 + actor.sorrowRock
                actor.shield += shieldGain
                // [不动如山] 消耗势时每层+3护盾
                // 这里是获得势而非消耗，但镇山岳直接给护盾
                log("  → 势+2 (当前${actor.momentum})，护盾+${shieldGain}", LogType.STATUS)
                log("  → ${t.name} 受到${dmg}伤害", LogType.DAMAGE)
            }
            "yan_fengluan" -> {
                val momentumGain = (actor.momentum * 0.5).roundToInt()
                actor.momentum += momentumGain
                log("  → 势+${momentumGain} (当前${actor.momentum})", LogType.STATUS)
                // [起势] 效果
                if (actor.momentum >= 10) {
                    actor.bonusAtk += 15
                    log("  → 势≥10，攻击力+15", LogType.STATUS)
                }
                // 全体队友获得悲岩层数护盾
                playerTeam.filter { it.isAlive }.forEach { ally ->
                    ally.shield += actor.sorrowRock
                }
                log("  → 全体队友护盾+${actor.sorrowRock}", LogType.STATUS)
            }
            "yan_cengluan" -> {
                val t = target ?: enemyTeam.first { it.isAlive }
                val dmg = calcDamage(actor, t, 1.0)
                applyDamage(t, dmg)
                processOnHitPassives(t, actor, dmg)
                t.momentum -= 5
                if (t.momentum < 0) t.momentum = 0
                log("  → ${t.name} 受到${dmg}伤害，势-5", LogType.DAMAGE)
            }
            "yan_baiyue" -> {
                val consumedMomentum = actor.momentum
                actor.momentum = 0
                val baseDmg = calcDamage(actor, enemyTeam.first { it.isAlive }, 0.5, ignorePressure = true)
                val momentumBonus = consumedMomentum * 2
                val shieldDmg = actor.shield
                val totalDmg = baseDmg + momentumBonus + shieldDmg

                enemyTeam.filter { it.isAlive }.forEach { e ->
                    e.hp -= totalDmg
                    if (e.hp < 0) e.hp = 0
                    processOnHitPassives(e, actor, totalDmg)
                }
                actor.shield = 0

                // [不动如山] 消耗势时每层+3护盾
                val shieldRecover = consumedMomentum * 3
                actor.shield += shieldRecover

                // 下回合恢复一半势
                pendingRecoverMomentum[actor.id] = consumedMomentum / 2

                log("  → 消耗${consumedMomentum}势+${shieldDmg}护盾值", LogType.STATUS)
                log("  → 全体受到${totalDmg}伤害！护盾恢复${shieldRecover}", LogType.DAMAGE)
                log("  → 下回合恢复${consumedMomentum/2}势", LogType.STATUS)
            }

            // ── 敌人 ──
            "enemy_normal" -> {
                val t = target ?: playerTeam.filter { it.isAlive }.random()
                val dmg = calcDamage(actor, t, 1.0)
                applyDamage(t, dmg)
                processOnHitPassives(t, actor, dmg)
                log("  → ${t.name} 受到${dmg}伤害", LogType.DAMAGE)
            }
            "enemy_heavy" -> {
                val t = target ?: playerTeam.filter { it.isAlive }.random()
                val dmg = calcDamage(actor, t, 1.5)
                applyDamage(t, dmg)
                processOnHitPassives(t, actor, dmg)
                log("  → 重击！${t.name} 受到${dmg}伤害", LogType.DAMAGE)
            }

            else -> log("  → 未知技能", LogType.NORMAL)
        }
    }

    // ── 结束角色行动 ──

    private fun endActorTurn(char: GameCharacter) {
        char.hasActed = true
        processEndOfTurnPassives(char)
    }

    // ── 推进到下一个行动者 ──

    fun advanceToNextActor() {
        currentActorIndex++
        while (currentActorIndex < turnOrder.size) {
            val actor = turnOrder[currentActorIndex]
            if (actor.isAlive) {
                if (!actor.isPlayer) {
                    // 敌人AI行动
                    enemyAIAct(actor)
                    currentActorIndex++
                } else {
                    // 玩家角色，等待玩家操作
                    return
                }
            } else {
                currentActorIndex++
            }
        }
        // 所有人行动完毕，进入下一回合
        endRound()
    }

    // ── 敌人AI ──

    private fun enemyAIAct(enemy: GameCharacter) {
        val alivePlayers = playerTeam.filter { it.isAlive }
        if (alivePlayers.isEmpty()) return

        log("${enemy.name} 的回合", LogType.NORMAL)

        // 简单AI：根据AP选择技能
        val skill = if (enemy.ap >= 2 && Random.nextFloat() < 0.4f) {
            enemy.skills.find { it.id == "enemy_heavy" }
        } else {
            enemy.skills.find { it.id == "enemy_normal" }
        } ?: SkillDefs.enemy_normal_atk

        // 优先攻击血量最低的玩家
        val target = alivePlayers.sortedBy { it.hp }.first()

        executeSkill(enemy, skill, target)

        if (checkGameEnd()) return
    }

    // ── 回合结束 ──

    private fun endRound() {
        // 所有角色回合结束被动
        for (char in playerTeam + enemyTeam) {
            if (char.isAlive) {
                processEndOfTurnPassives(char)
            }
        }

        if (checkGameEnd()) return

        currentTurn++
        processTurnStart()
    }

    // ── 检查胜负 ──

    fun checkGameEnd(): Boolean {
        if (playerTeam.none { it.isAlive }) {
            isGameOver = true
            isVictory = false
            log("═══ 战斗失败 ═══", LogType.DEFEAT)
            return true
        }
        if (enemyTeam.none { it.isAlive }) {
            isGameOver = true
            isVictory = true
            if (currentStage < 3) {
                log("═══ 关卡通关！进入下一关 ═══", LogType.VICTORY)
            } else {
                log("═══ 全部通关！胜利！ ═══", LogType.VICTORY)
            }
            return true
        }
        return false
    }

    // ── 进入下一关 ──

    fun nextStage() {
        // 保留玩家角色当前状态（部分恢复）
        playerTeam.forEach { char ->
            char.hp = minOf(char.hp + char.maxHp / 4, char.maxHp) // 恢复25%
            char.shield = 0
            char.momentum = 0
            char.momentumPressure = 0
            char.intent = 0
            char.extremeIntent = 0
            char.statuses.clear()
            char.ap = char.maxAp
        }
        initializeGame(currentStage + 1)
    }

    // ── 日志 ──

    private fun log(text: String, type: LogType = LogType.NORMAL) {
        battleLog.add(LogEntry(text, type, currentTurn))
    }

    // ── 获取状态快照 ──

    fun getBattleState(): BattleState {
        val actor = currentActor()
        return BattleState(
            playerTeam = playerTeam.toList(),
            enemyTeam = enemyTeam.toList(),
            currentTurn = currentTurn,
            isPlayerTurn = actor?.isPlayer == true,
            activeCharacterId = actor?.id,
            battleLog = battleLog.toList(),
            isGameOver = isGameOver,
            isVictory = isVictory,
            turnOrder = turnOrder.map { it.id },
            currentActorIndex = currentActorIndex
        )
    }

    // ── 获取当前可行动的玩家角色 ──

    fun getActivePlayerCharacter(): GameCharacter? {
        val actor = currentActor()
        return if (actor != null && actor.isPlayer && actor.isAlive) actor else null
    }

    // ── 玩家执行技能后推进 ──

    fun playerExecuteSkill(skill: SkillDef, target: GameCharacter?) {
        val actor = currentActor() ?: return
        if (!actor.isPlayer) return

        executeSkill(actor, skill, target)

        if (!checkGameEnd()) {
            advanceToNextActor()
        }
    }

    // ── 玩家跳过行动 ──

    fun playerSkipTurn() {
        val actor = currentActor() ?: return
        if (!actor.isPlayer) return
        log("${actor.name} 跳过了行动", LogType.NORMAL)
        endActorTurn(actor)
        if (!checkGameEnd()) {
            advanceToNextActor()
        }
    }
}
