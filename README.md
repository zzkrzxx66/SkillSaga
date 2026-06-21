# SkillSaga - 技能传说

一款基于回合制卡牌/RPG战斗系统的Android游戏，灵感来源于手写的游戏角色技能设计草案。

## 游戏特色

### 三大角色，各具特色
- **剑王·大海**：近战输出 / 势能成长型
  - 核心机制：「势」层层叠加，回合越久越强
  - 特色技能：万江归海（AOE自动触发）、月海潮生（暴击爆发）

- **隐月·薇**：高爆发刺客 / 暴击特化型
  - 核心机制：「意」瞬间积攒，毁灭性爆发
  - 特色技能：明静心决（低血量触发极意）、天威明剑决（AOE连击）

- **破军·岩**：坦克 / 护盾辅助型
  - 核心机制：「势」与护盾，承受伤害转化为团队防护
  - 特色技能：拜岳撼天（消耗所有势和护盾的爆发）

### 战斗系统
- **回合制战斗**：速度决定行动顺序
- **行动点系统**：技能消耗/回复行动点
- **护盾机制**：优先承受伤害，保护生命值
- **状态叠加**：各种增益/减益效果以层数叠加

### 技能设计
- **自动触发**：满足条件自动发动的技能
- **资源联动**：技能与角色专属资源（势/意/悲岩）深度绑定
- **决策点**：衍生技、条件触发、资源管理

## 技术栈

- **语言**：Kotlin
- **UI框架**：Jetpack Compose
- **架构**：MVVM
- **构建工具**：Gradle 8.5
- **CI/CD**：GitHub Actions

## 快速开始

### 环境要求
- Android Studio Hedgehog | 2023.1.1 或更高
- JDK 17
- Android SDK 34

### 构建与运行
1. 克隆仓库
   ```bash
   git clone https://github.com/your-username/SkillSaga.git
   ```

2. 用Android Studio打开项目

3. 连接Android设备或启动模拟器

4. 点击运行按钮或使用命令行：
   ```bash
   ./gradlew installDebug
   ```

### 使用GitHub Actions自动编译
1. Fork本仓库
2. 在仓库设置中启用GitHub Actions
3. 推送代码到main分支，Actions会自动编译APK
4. 在Actions页面下载编译好的APK

## 项目结构

```
SkillSaga/
├── .github/workflows/    # GitHub Actions配置
├── app/                  # 主应用模块
│   ├── src/main/java/   # Kotlin源代码
│   │   └── com/skillsaga/app/
│   │       ├── Character.kt      # 角色数据模型
│   │       ├── GameEngine.kt     # 战斗引擎
│   │       └── MainActivity.kt   # 主界面
│   └── src/main/res/    # 资源文件
├── build.gradle.kts     # 项目级构建脚本
├── settings.gradle.kts  # 项目设置
└── gradle.properties    # Gradle配置
```

## 游戏设计文档

详细的角色技能设计请参考 [game-design-doc.md](game-design-doc.md)

## 路线图

### v1.0 (当前版本)
- [x] 基础战斗系统
- [x] 三个可玩角色
- [x] 技能系统实现
- [x] 简单的AI对手
- [x] 战斗日志

### v2.0 (计划中)
- [ ] 角色选择界面
- [ ] 更丰富的敌人类型
- [ ] 技能动画效果
- [ ] 音效和背景音乐
- [ ] 存档系统
- [ ] 多语言支持

### v3.0 (远期目标)
- [ ] 多人对战模式
- [ ] 角色养成系统
- [ ] 剧情模式
- [ ] 排行榜系统

## 贡献指南

欢迎提交Issue和Pull Request！

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## 许可证

本项目采用MIT许可证 - 详见 [LICENSE](LICENSE) 文件

## 致谢

- 感谢手写游戏设计草案的创作者
- 感谢Jetpack Compose和Kotlin社区
- 感谢所有测试玩家

## 联系方式

- 项目链接：https://github.com/your-username/SkillSaga
- 问题反馈：Issues

---

**注意**：这是一个演示项目，用于展示回合制RPG战斗系统的实现。游戏平衡性、视觉效果和用户体验需要进一步优化。