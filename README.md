## About
# Kaleidoscope World Liquor (Fabric)
# 森罗酒馆：世界名酒 - Fabric 移植

> A Minecraft Fabric mod that brings the world's most famous liquors and cocktails — from Bombay Sapphire Gin to Feitian Moutai — to Kaleidoscope Tavern.

## Compendium
- This is the **Fabric port** of [**森罗酒馆：世界名酒**](https://www.mcmod.cn/class/25477.html) (Kaleidoscope World Liquor)**, corresponding to version `1.1.10`.
- Requires [**森罗物语：酒馆**](https://www.mcmod.cn/class/25200.html) (Kaleidoscope Tavern) and [**Forge Config API Port**](https://modrinth.com/mod/forge-config-api-port).

## Overview
![Minecraft](https://img.shields.io/badge/Minecraft-Java%20Edition-brightgreen)
![Fabric](https://img.shields.io/badge/1.21.11-orange)
![License](https://img.shields.io/badge/License-MIT_+_CC_BY--NC--ND_4.0-lightgrey)

## Content
- **52 real-world liquors & cocktails**: Bombay Sapphire Gin (孟买蓝宝石金酒), Jack Daniel's (杰克丹尼), Smirnoff Red Vodka (斯米诺红牌伏特加), Absolut Vodka (绝对伏特加), Piña Colada, Moutai (飞天茅台), Bacardí Carta Blanca (百加得白朗姆), Spirytus 96 (生命之水96), SKYY Vodka (深蓝伏特加), Johnnie Walker (尊尼获加), Lafite 1982 (拉菲1982), Strongbow (诗庄堡), Dassai (獺祭), Kvass (格瓦斯), Bamboo Leaf Green (竹叶青), and cocktails such as Around the World (环游世界), Long Island Iced Tea (长岛冰茶), Piña Colada (椰林飘香), Gin Tonic (金汤力), Jerk (渣男), Shrimp Cocktail (鲜虾鸡尾酒), and more.
- **Shaker (雪克杯)**: mix colored cocktail ingredients into shakes and cocktails.
- **Freezer (冰柜)**: pour in any fluid bucket plus up to 4 ingredients to freeze — ice from water, magma blocks from lava, and integration snacks. Fully automated with hoppers and pipes (item input/output from any side, product extraction from the bottom, fluid transfer from any side).
- **Bar furniture**: bar stools in 16 colors, bar cabinets and cellar cabinets (place your liquor bottles inside, visible in 3D), bar counter.
- **Wall record & custom disc (墙面唱片)**: mount any music disc — including the mod's own bar-style disc — on a wall.
- **Status effects & enchantment**: Tequila (龙舌兰), Captain's Blessing (船长的祝福), Treasure Guide (淘金热), Multi Jump (多段跳), Beheading (斩首), Ground Crit (破势), Heavy Slash (重斩) and more; Brew Accelerator (臻酿) enchantment speeds up barrel brewing.
- **Paintings**: 8 community author paintings.

## Compat
- **JEI**: view freezer recipes (shaker / barrel / pressing tub recipes are provided by Kaleidoscope Tavern itself).
- **Jade**: shows the freezer's remaining time.
- **SMC (星喵工艺)**: Iced Tea (劲凉冰红茶) brew block + item.
- **Kaleidoscope Twilight (孤独摇滚联动)**: Pochi Pudding (波奇布丁), Kita Stuffed Crisp (喜多夹心脆), Liangshan Ice Cone (凉山冰锥), Magic Crispy Corner (妙脆角) — obtainable via the freezer, plus 6 contributor dolls.
- **Kaleidoscope Doll (森罗物语：玩偶)**: 6 contributor dolls (doll_0 ~ doll_5) with author tooltips, craftable from pink wool in a stonecutter; feed them Pochi Pudding for a surprise.

## Build
Requires JDK 21.

Dependency jars (Kaleidoscope Tavern, Kaleidoscope Doll) are committed under `libs/` and referenced directly by `build.gradle`.

```
./gradlew build
```

The artifact is produced in `build/libs/`.

## License
This project is a **Fabric port** of the original Kaleidoscope World Liquor (森罗酒馆：世界名酒). Code is licensed under **MIT**, assets under **CC BY-NC-ND 4.0** — see [LICENSE-CODE](LICENSE-CODE) and [LICENSE-ASSETS](LICENSE-ASSETS).

Original mod by 白馒头 (BmtUltra), 辰笺渡月 (chenjdy / ChenjdyUltra), 白帆小喵L (Bfxm). Fabric port by [Dobelring](https://github.com/Dobelring).
