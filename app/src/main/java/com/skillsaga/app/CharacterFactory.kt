package com.skillsaga.app

import kotlin.random.Random

// ═══════════════════════════════════════════════════════════
//  角色工厂
// ═══════════════════════════════════════════════════════════

object CharacterFactory {

    fun createDaHai(): GameCharacter = GameCharacter(
        id = "dahai", name = "大海", title = "剑王", role = Role.DPS,
        hp = 160, maxHp = 160, atk = 20, speed = 100,
        ap = 6, maxAp = 10, shield = 0,
        momentum = 0, momentumPressure = 0, intent = 0, extremeIntent = 0, sorrowRock = 0,
        skills = SkillDefs.dahai_skills
    )

    fun createWei(): GameCharacter = GameCharacter(
        id = "wei", name = "薇", title = "隐月", role = Role.ASSASSIN,
        hp = 130, maxHp = 130, atk = 25, speed = 110,
        ap = 6, maxAp = 10, shield = 0,
        momentum = 0, momentumPressure = 0, intent = 0, extremeIntent = 0, sorrowRock = 0,
        baseCritRate = 10, baseCritDmg = 160,
        skills = SkillDefs.wei_skills
    )

    fun createYan(): GameCharacter = GameCharacter(
        id = "yan", name = "岩", title = "破军", role = Role.TANK,
        hp = 180, maxHp = 180, atk = 10, speed = 85,
        ap = 6, maxAp = 10, shield = 0,
        momentum = 0, momentumPressure = 0, intent = 0, extremeIntent = 0, sorrowRock = 3,
        skills = SkillDefs.yan_skills
    )

    // ── 敌人 ──

    fun createGoblin(): GameCharacter = GameCharacter(
        id = "goblin", name = "哥布林战士", title = "杂兵", role = Role.DPS,
        hp = 80, maxHp = 80, atk = 12, speed = 90,
        ap = 3, maxAp = 5, shield = 0,
        momentum = 0, momentumPressure = 0, intent = 0, extremeIntent = 0, sorrowRock = 0,
        skills = listOf(SkillDefs.enemy_normal_atk),
        isPlayer = false, enemyAtk = 12
    )

    fun createOrc(): GameCharacter = GameCharacter(
        id = "orc", name = "兽人狂战士", title = "精英", role = Role.DPS,
        hp = 130, maxHp = 130, atk = 16, speed = 75,
        ap = 4, maxAp = 6, shield = 10,
        momentum = 0, momentumPressure = 0, intent = 0, extremeIntent = 0, sorrowRock = 0,
        skills = listOf(SkillDefs.enemy_normal_atk, SkillDefs.enemy_heavy_atk),
        isPlayer = false, enemyAtk = 16
    )

    fun createDarkMage(): GameCharacter = GameCharacter(
        id = "dark_mage", name = "暗黑法师", title = "精英", role = Role.ASSASSIN,
        hp = 100, maxHp = 100, atk = 20, speed = 105,
        ap = 5, maxAp = 7, shield = 0,
        momentum = 0, momentumPressure = 0, intent = 0, extremeIntent = 0, sorrowRock = 0,
        baseCritRate = 15, baseCritDmg = 170,
        skills = listOf(SkillDefs.enemy_normal_atk, SkillDefs.enemy_heavy_atk),
        isPlayer = false, enemyAtk = 20
    )

    fun createBoss(): GameCharacter = GameCharacter(
        id = "boss", name = "深渊领主", title = "BOSS", role = Role.TANK,
        hp = 250, maxHp = 250, atk = 22, speed = 95,
        ap = 5, maxAp = 8, shield = 20,
        momentum = 0, momentumPressure = 0, intent = 0, extremeIntent = 0, sorrowRock = 0,
        skills = listOf(SkillDefs.enemy_normal_atk, SkillDefs.enemy_heavy_atk),
        isPlayer = false, enemyAtk = 22
    )

    /** 根据关卡创建敌人编队 */
    fun createEnemyTeam(stage: Int): List<GameCharacter> = when (stage) {
        1 -> listOf(createGoblin(), createGoblin(), createOrc())
        2 -> listOf(createOrc(), createDarkMage(), createOrc())
        3 -> listOf(createDarkMage(), createDarkMage(), createBoss())
        else -> listOf(createGoblin(), createGoblin(), createGoblin())
    }

    /** 获取角色头像emoji */
    fun getAvatar(char: GameCharacter): String = when (char.id) {
        "dahai" -> "🌊"
        "wei" -> "🌙"
        "yan" -> "⛰️"
        "goblin" -> "👺"
        "orc" -> "👹"
        "dark_mage" -> "🧙"
        "boss" -> "💀"
        else -> "❓"
    }

    /** 角色颜色 */
    fun getColor(char: GameCharacter): Long = when (char.id) {
        "dahai" -> 0xFF1976D2
        "wei" -> 0xFF9C27B0
        "yan" -> 0xFF795548
        "goblin" -> 0xFF4CAF50
        "orc" -> 0xFFFF5722
        "dark_mage" -> 0xFF673AB7
        "boss" -> 0xFFD32F2F
        else -> 0xFF607D8B
    }
}
