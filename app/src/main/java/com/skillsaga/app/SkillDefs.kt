package com.skillsaga.app

// ═══════════════════════════════════════════════════════════
//  技能定义 - 完整实现设计文档中所有技能
// ═══════════════════════════════════════════════════════════

object SkillDefs {

    // ────────────── 剑王·大海 ──────────────

    val dahai_skills = listOf(
        SkillDef(
            id = "dahai_zhanchao",
            name = "斩潮式",
            type = SkillType.BASIC,
            description = "对单体敌方造成1×1伤害，「势」+1",
            targetType = TargetType.SINGLE_ENEMY,
            apCost = 0,
            endsTurn = true
        ),
        SkillDef(
            id = "dahai_dielang",
            name = "叠浪式",
            type = SkillType.DERIVED,
            description = "连续使用不结束回合：造成1×0.5伤害，每回合第x次使用时势+x(x≥3后+0)",
            targetType = TargetType.SINGLE_ENEMY,
            apCost = -1,   // 回复1⚪
            endsTurn = false
        ),
        SkillDef(
            id = "dahai_yuehai",
            name = "月海潮生",
            type = SkillType.SKILL_3,
            description = "暴击率+20%，暴伤+50%，获得「海潮」（势不减少）",
            targetType = TargetType.SELF,
            apCost = 5,
            endsTurn = false,
            baseCritBonus = 20,
            baseCritDmgBonus = 50
        )
    )

    val dahai_auto = SkillDef(
        id = "dahai_wanjiang",
        name = "万江归海",
        type = SkillType.AUTO,
        description = "势≥10时自动发动：势-10，对全体敌方造成1×1伤害",
        targetType = TargetType.ALL_ENEMIES,
        apCost = 0,
        endsTurn = false,
        isAOE = true
    )

    // ────────────── 隐月·薇 ──────────────

    val wei_skills = listOf(
        SkillDef(
            id = "wei_mingxiao",
            name = "明霄剑气",
            type = SkillType.SKILL_1,
            description = "获得1层「附魔」(攻击附加原伤害×0.5)+1层「反击」(受击反击1×0.8)",
            targetType = TargetType.SELF,
            apCost = 1,
            endsTurn = false,
            maxUsesPerTurn = 2
        ),
        SkillDef(
            id = "wei_huanyun",
            name = "幻云斩",
            type = SkillType.DERIVED,
            description = "造成1×1伤害，意+1。⚪≤0时+2⚪；⚪>0时获得等同⚪数量的护盾",
            targetType = TargetType.SINGLE_ENEMY,
            apCost = -1,  // 回复1⚪
            endsTurn = true
        ),
        SkillDef(
            id = "wei_mingjing",
            name = "明静心决",
            type = SkillType.SKILL_2,
            description = "意+3，暴伤+50%(2回合)。♡≤50%时获得「极意」(恢复已损♡×0.5)",
            targetType = TargetType.SELF,
            apCost = 2,
            endsTurn = false,
            maxUsesPerTurn = 1,
            baseCritDmgBonus = 50
        ),
        SkillDef(
            id = "wei_tianwei",
            name = "天威明剑决",
            type = SkillType.SKILL_3,
            description = "全体1×0.8伤害×2次+随机目标意×1固有伤害。极意时额外全体1×1+意+3",
            targetType = TargetType.ALL_ENEMIES,
            apCost = 3,
            endsTurn = true,
            requiresIntent = 3,
            isAOE = true
        )
    )

    // ────────────── 破军·岩 ──────────────

    val yan_skills = listOf(
        SkillDef(
            id = "yan_zhenshan",
            name = "镇山岳",
            type = SkillType.BASIC,
            description = "造成1×0.8伤害(不受势压影响)，势+2，获得10+悲岩层数的护盾",
            targetType = TargetType.SINGLE_ENEMY,
            apCost = -1,  // 回复1⚪
            endsTurn = true
        ),
        SkillDef(
            id = "yan_fengluan",
            name = "峰峦起",
            type = SkillType.SKILL_2,
            description = "获得当前势×0.5的势，全体队友获得悲岩层数的护盾",
            targetType = TargetType.ALL_ALLIES,
            apCost = 1,
            endsTurn = false
        ),
        SkillDef(
            id = "yan_cengluan",
            name = "层峦叠嶂",
            type = SkillType.DERIVED,
            description = "造成1×1伤害，目标势-5",
            targetType = TargetType.SINGLE_ENEMY,
            apCost = 1,
            endsTurn = false
        ),
        SkillDef(
            id = "yan_baiyue",
            name = "拜岳撼天",
            type = SkillType.SKILL_3,
            description = "消耗全部势：全体1×0.5+每层势额外+2+当前护盾值等额伤害。下回合恢复一半势",
            targetType = TargetType.ALL_ENEMIES,
            apCost = 3,
            endsTurn = true,
            isAOE = true
        )
    )

    // ────────────── 敌人技能 ──────────────

    val enemy_normal_atk = SkillDef(
        id = "enemy_normal",
        name = "普通攻击",
        type = SkillType.BASIC,
        description = "对单体造成1×1伤害",
        targetType = TargetType.SINGLE_ENEMY,
        apCost = 0,
        endsTurn = true
    )

    val enemy_heavy_atk = SkillDef(
        id = "enemy_heavy",
        name = "重击",
        type = SkillType.BASIC,
        description = "对单体造成1.5×1伤害",
        targetType = TargetType.SINGLE_ENEMY,
        apCost = 2,
        endsTurn = true
    )
}
