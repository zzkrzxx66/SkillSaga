package com.skillsaga.app

class GameEngine {
    var playerTeam: MutableList<Character> = mutableListOf()
    var enemyTeam: MutableList<Character> = mutableListOf()
    var currentTurn: Int = 1
    var isPlayerTurn: Boolean = true
    var gameLog: MutableList<String> = mutableListOf()
    var gameState: GameState? = null

    fun initializeGame() {
        // Create player team (user selects 3 characters)
        playerTeam = mutableListOf(
            CharacterFactory.createDaHai(),
            CharacterFactory.createWei(),
            CharacterFactory.createYan()
        )
        
        // Create enemy team (simple enemies for now)
        enemyTeam = mutableListOf(
            createEnemy("goblin", "哥布林", 80, 12, 90),
            createEnemy("orc", "兽人", 120, 15, 80),
            createEnemy("dark_mage", "暗黑法师", 100, 18, 95)
        )
        
        // Sort by speed for turn order
        playerTeam.sortByDescending { it.speed }
        enemyTeam.sortByDescending { it.speed }
        
        // Initialize game state
        gameState = GameState(playerTeam, enemyTeam)
        
        gameLog.add("战斗开始！")
        gameLog.add("你的队伍：${playerTeam.joinToString { "${it.title}·${it.name}" }}")
        gameLog.add("敌方队伍：${enemyTeam.joinToString { it.name }}")
    }

    private fun createEnemy(id: String, name: String, hp: Int, atk: Int, speed: Int): Character {
        return Character(
            id = id,
            name = name,
            title = "",
            hp = hp,
            maxHp = hp,
            atk = atk,
            speed = speed,
            ap = 3,
            maxAp = 5,
            shield = 0,
            momentum = 0,
            intent = 0,
            sorrowRock = 0,
            skills = emptyList(),
            isPlayer = false
        )
    }

    fun executeSkill(character: Character, skill: Skill, target: Character) {
        if (!canUseSkill(character, skill)) {
            gameLog.add("${character.name} 无法使用 ${skill.name}")
            return
        }
        
        // Apply AP cost
        character.ap -= skill.apCost
        if (character.ap < 0) character.ap = 0
        if (character.ap > character.maxAp) character.ap = character.maxAp
        
        // Record skill use
        gameState?.recordSkillUse(character.id, skill.id)
        
        // Execute skill effect
        skill.effect(character, target, gameState!!)
        
        // Check for auto-triggered skills (like 万江归海)
        checkAutoTriggeredSkills(character)
        
        // End turn if skill ends turn
        if (skill.endsTurn) {
            endCharacterTurn(character)
        }
        
        gameLog.add("${character.name} 使用了 ${skill.name}")
    }

    private fun checkAutoTriggeredSkills(character: Character) {
        if (character.id == "dahai" && character.momentum >= 10) {
            // Auto-trigger 万江归海
            val skill = character.skills.find { it.id == "dahai_skill2" }
            if (skill != null) {
                skill.effect(character, enemyTeam.first(), gameState!!)
                gameLog.add("${character.name} 自动发动万江归海！")
            }
        }
    }

    private fun canUseSkill(character: Character, skill: Skill): Boolean {
        // Check AP cost
        if (character.ap < skill.apCost) return false
        
        // Check max uses per turn
        if (skill.maxUsesPerTurn != null) {
            val currentUses = gameState?.getSkillUsesThisTurn(character.id, skill.id) ?: 0
            if (currentUses >= skill.maxUsesPerTurn) return false
        }
        
        // Check conditions
        if (skill.requiresCondition != null) {
            when (skill.requiresCondition) {
                "momentum>=10" -> if (character.momentum < 10) return false
                "intent>=3" -> if (character.intent < 3) return false
            }
        }
        
        return true
    }

    private fun endCharacterTurn(character: Character) {
        // Process end-of-turn effects
        processStatusEffects(character)
        
        // Check for auto-triggered skills at turn end
        if (character.id == "dahai" && character.momentum >= 10) {
            checkAutoTriggeredSkills(character)
        }
    }

    private fun processStatusEffects(character: Character) {
        val statusesToRemove = mutableListOf<Status>()
        
        for (status in character.statuses) {
            when (status.id) {
                "sea_tide" -> {
                    // 海潮：势不会减少，每回合结束-1
                    if (status.duration != null) {
                        status.stacks - 1
                        if (status.stacks <= 0) {
                            statusesToRemove.add(status)
                        }
                    }
                }
                "crit_up", "crit_dmg_up" -> {
                    // Duration-based statuses
                    if (status.duration != null) {
                        status.stacks - 1
                        if (status.stacks <= 0) {
                            statusesToRemove.add(status)
                        }
                    }
                }
            }
        }
        
        character.statuses.removeAll(statusesToRemove)
    }

    fun aiTurn() {
        // Simple AI for enemies
        for (enemy in enemyTeam) {
            if (enemy.hp <= 0) continue
            
            // Choose random alive player character to attack
            val alivePlayers = playerTeam.filter { it.hp > 0 }
            if (alivePlayers.isEmpty()) break
            
            val target = alivePlayers.random()
            val damage = enemy.atk
            
            // Apply damage to shield first, then HP
            if (target.shield >= damage) {
                target.shield -= damage
                gameLog.add("${enemy.name} 攻击 ${target.name}，护盾吸收 $damage 伤害")
            } else {
                val remainingDamage = damage - target.shield
                target.shield = 0
                target.hp -= remainingDamage
                gameLog.add("${enemy.name} 攻击 ${target.name}，造成 $remainingDamage 伤害")
            }
            
            // Check for counter attack (if target has 反击 status)
            val counterStatus = target.statuses.find { it.id == "counter" }
            if (counterStatus != null && counterStatus.stacks > 0) {
                val counterDamage = (target.atk * 0.8).toInt()
                enemy.hp -= counterDamage
                counterStatus.stacks - 1
                gameLog.add("${target.name} 反击！对 ${enemy.name} 造成 $counterDamage 伤害")
            }
        }
        
        // Check for victory/defeat
        checkGameEnd()
    }

    fun startNewTurn() {
        currentTurn++
        isPlayerTurn = true
        gameLog.add("--- 第 $currentTurn 回合 ---")
        
        // Reset AP for all characters
        for (character in playerTeam + enemyTeam) {
            character.ap = character.maxAp
        }
        
        // Process start-of-turn effects
        for (character in playerTeam) {
            processStartOfTurn(character)
        }
        
        // Reset skill uses for the turn
        gameState?.resetTurn()
    }

    private fun processStartOfTurn(character: Character) {
        // Process start-of-turn statuses
        // For example, 海潮 prevents 势 from decreasing
        val hasSeaTide = character.statuses.any { it.id == "sea_tide" }
        
        // 大海's 潮汐 passive
        if (character.id == "dahai") {
            if (character.momentum < 10) {
                character.momentum += 3
                gameLog.add("${character.name} 的潮汐被动：势+3")
            }
        }
        
        // 薇's passive: if has 意, attack +5% extra damage, crit +20%
        // This is handled in damage calculation
    }

    fun checkGameEnd(): Boolean {
        val allPlayerDead = playerTeam.all { it.hp <= 0 }
        val allEnemyDead = enemyTeam.all { it.hp <= 0 }
        
        if (allPlayerDead) {
            gameLog.add("战斗失败！")
            return true
        }
        
        if (allEnemyDead) {
            gameLog.add("战斗胜利！")
            return true
        }
        
        return false
    }

    fun getBattleState(): BattleState {
        return BattleState(
            playerTeam = playerTeam.toList(),
            enemyTeam = enemyTeam.toList(),
            currentTurn = currentTurn,
            isPlayerTurn = isPlayerTurn,
            gameLog = gameLog.toList()
        )
    }
}

data class BattleState(
    val playerTeam: List<Character>,
    val enemyTeam: List<Character>,
    val currentTurn: Int,
    val isPlayerTurn: Boolean,
    val gameLog: List<String>
)