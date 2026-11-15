package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class BubbleType(
    val color: Color,
    val glowColor: Color,
    val isColorMatchable: Boolean = true,
    val isSpecial: Boolean = false
) {
    RED(BubbleRed, Color(0xFFFF8FA3)),
    BLUE(BubbleBlue, Color(0xFF82B1FF)),
    GREEN(BubbleGreen, Color(0xFF69F0AE)),
    YELLOW(BubbleYellow, Color(0xFFFFEA70)),
    PURPLE(BubblePurple, Color(0xFFE09EFF)),
    CYAN(BubbleCyan, Color(0xFF84FFFF)),
    STONE(BubbleStone, Color(0xFF9CA3AF), isColorMatchable = false, isSpecial = false),
    ICE(BubbleIce, Color(0xFFE0F7FA), isColorMatchable = false, isSpecial = false),
    RAINBOW(Color.White, SunlitGold, isColorMatchable = false, isSpecial = true),
    BOMB(Color(0xFF2B2D42), CoralPop, isColorMatchable = false, isSpecial = true),
    LIGHTNING(Color(0xFF00B4D8), ElectricCyan, isColorMatchable = false, isSpecial = true),
    SUPERNOVA(Color(0xFFFF9F1C), Color(0xFFFFD166), isColorMatchable = false, isSpecial = true);

    companion object {
        val standardColors = listOf(RED, BLUE, GREEN, YELLOW, PURPLE, CYAN)
    }
}

enum class LevelThemeId {
    CORAL_LAGOON,
    EMERALD_CANOPY,
    MAGMA_CAVERN,
    STARLIGHT_NEBULA
}

data class LevelThemeConfig(
    val id: LevelThemeId,
    val title: String,
    val subtitle: String,
    val skyTop: Color,
    val skyMid: Color,
    val skyBottom: Color,
    val accent: Color,
    val cardGradientStart: Color,
    val cardGradientEnd: Color,
    val milestoneSkinId: SkinId
)

object LevelThemes {
    val all: Map<LevelThemeId, LevelThemeConfig> = mapOf(
        LevelThemeId.CORAL_LAGOON to LevelThemeConfig(
            id = LevelThemeId.CORAL_LAGOON,
            title = "Coral Lagoon",
            subtitle = "Sunlit reefs & gentle ocean tides",
            skyTop = Color(0xFF042A4A),
            skyMid = Color(0xFF085078),
            skyBottom = Color(0xFF0D7C8C),
            accent = ElectricCyan,
            cardGradientStart = Color(0xFF064266),
            cardGradientEnd = Color(0xFF0E7B8C),
            milestoneSkinId = SkinId.FOREST_SPRITE
        ),
        LevelThemeId.EMERALD_CANOPY to LevelThemeConfig(
            id = LevelThemeId.EMERALD_CANOPY,
            title = "Emerald Canopy",
            subtitle = "Enchanted groves & stone guardians",
            skyTop = Color(0xFF072A1E),
            skyMid = Color(0xFF0E4D36),
            skyBottom = Color(0xFF1C734B),
            accent = EmeraldMint,
            cardGradientStart = Color(0xFF0B3B2A),
            cardGradientEnd = Color(0xFF1A6945),
            milestoneSkinId = SkinId.EMBER_DRAGON
        ),
        LevelThemeId.MAGMA_CAVERN to LevelThemeConfig(
            id = LevelThemeId.MAGMA_CAVERN,
            title = "Magma Cavern",
            subtitle = "Volcanic embers & frozen crystal locks",
            skyTop = Color(0xFF280917),
            skyMid = Color(0xFF5E1426),
            skyBottom = Color(0xFF9E2A1C),
            accent = CoralPop,
            cardGradientStart = Color(0xFF3E0D1B),
            cardGradientEnd = Color(0xFF8C2418),
            milestoneSkinId = SkinId.NEBULA_VALKYRIE
        ),
        LevelThemeId.STARLIGHT_NEBULA to LevelThemeConfig(
            id = LevelThemeId.STARLIGHT_NEBULA,
            title = "Starlight Nebula",
            subtitle = "Cosmic auroras & grand constellation trials",
            skyTop = Color(0xFF12082E),
            skyMid = Color(0xFF2B125E),
            skyBottom = Color(0xFF4F1D85),
            accent = RoyalViolet,
            cardGradientStart = Color(0xFF1D0D4A),
            cardGradientEnd = Color(0xFF471A78),
            milestoneSkinId = SkinId.GOLDEN_MONARCH
        )
    )
}

enum class BoosterType(
    val id: String,
    val title: String,
    val shortLabel: String,
    val description: String,
    val durationLabel: String,
    val coinPrice: Int,
    val isRare: Boolean,
    val accentColor: Color
) {
    EAGLE_EYE(
        id = "EAGLE_EYE",
        title = "Eagle Eye Sight",
        shortLabel = "Aim+",
        description = "Temporary 1-level power-up: extends your trajectory guide across 3 wall bounces.",
        durationLabel = "Temp: 1 Level",
        coinPrice = 120,
        isRare = false,
        accentColor = ElectricCyan
    ),
    DOUBLE_SCORE(
        id = "DOUBLE_SCORE",
        title = "2x Score Elixir",
        shortLabel = "2x Pts",
        description = "Temporary 1-level power-up: doubles all bubble pop and cascade drop points.",
        durationLabel = "Temp: 1 Level",
        coinPrice = 150,
        isRare = false,
        accentColor = SunlitGold
    ),
    EXTRA_SHOTS(
        id = "EXTRA_SHOTS",
        title = "+5 Orbs Pack",
        shortLabel = "+5 Orbs",
        description = "One-time boost: instantly adds 5 extra bubble shots to your current match.",
        durationLabel = "One-Time Use",
        coinPrice = 110,
        isRare = false,
        accentColor = EmeraldMint
    ),
    BOMB_ORB(
        id = "BOMB_ORB",
        title = "Blast Bomb Orb",
        shortLabel = "Bomb",
        description = "One-time special orb: detonates a wide 2-ring blast, shattering Stone & Ice.",
        durationLabel = "One-Time Shot",
        coinPrice = 100,
        isRare = false,
        accentColor = CoralPop
    ),
    LIGHTNING_ORB(
        id = "LIGHTNING_ORB",
        title = "Lightning Arc Orb",
        shortLabel = "Bolt",
        description = "One-time special orb: vaporizes an entire horizontal row in one strike.",
        durationLabel = "One-Time Shot",
        coinPrice = 140,
        isRare = false,
        accentColor = Color(0xFF38BDF8)
    ),
    RAINBOW_ORB(
        id = "RAINBOW_ORB",
        title = "Prism Rainbow Orb",
        shortLabel = "Prism",
        description = "One-time special orb: adapts to match and pop the largest touching color cluster.",
        durationLabel = "One-Time Shot",
        coinPrice = 160,
        isRare = false,
        accentColor = RoyalViolet
    ),
    SUPERNOVA_ORB(
        id = "SUPERNOVA_ORB",
        title = "Supernova Star",
        shortLabel = "Nova",
        description = "Rare one-time cosmic booster: clears a huge blast radius PLUS full row & cross!",
        durationLabel = "Rare 1-Time",
        coinPrice = 280,
        isRare = true,
        accentColor = Color(0xFFFF9F1C)
    );

    companion object {
        fun fromId(id: String): BoosterType? = entries.firstOrNull { it.id == id }
    }
}

enum class SkinId {
    CORAL_GUARDIAN,
    FOREST_SPRITE,
    EMBER_DRAGON,
    NEBULA_VALKYRIE,
    GOLDEN_MONARCH,
    PRISMATIC_PHOENIX
}

data class CharacterSkin(
    val id: SkinId,
    val name: String,
    val subtitle: String,
    val unlockHint: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val auraColor: Color,
    val isExclusive: Boolean = true
)

object CharacterSkins {
    val all: List<CharacterSkin> = listOf(
        CharacterSkin(
            id = SkinId.CORAL_GUARDIAN,
            name = "Coral Guardian",
            subtitle = "Pearlescent Tide Blaster",
            unlockHint = "Starter Skin",
            primaryColor = Color(0xFF00B4D8),
            secondaryColor = Color(0xFF03045E),
            accentColor = Color(0xFF90E0EF),
            auraColor = ElectricCyan,
            isExclusive = false
        ),
        CharacterSkin(
            id = SkinId.FOREST_SPRITE,
            name = "Forest Sprite",
            subtitle = "Emerald Firefly Launcher",
            unlockHint = "Complete Level 3 (Coral Lagoon Finale)",
            primaryColor = Color(0xFF2EC4B6),
            secondaryColor = Color(0xFF0B3B2A),
            accentColor = Color(0xFFA7F3D0),
            auraColor = EmeraldMint
        ),
        CharacterSkin(
            id = SkinId.EMBER_DRAGON,
            name = "Ember Dragon",
            subtitle = "Volcanic Wyrm Cannon",
            unlockHint = "Complete Level 6 (Emerald Canopy Finale)",
            primaryColor = Color(0xFFFF4D6D),
            secondaryColor = Color(0xFF590D22),
            accentColor = SunlitGold,
            auraColor = CoralPop
        ),
        CharacterSkin(
            id = SkinId.NEBULA_VALKYRIE,
            name = "Nebula Valkyrie",
            subtitle = "Astral Prism Wing",
            unlockHint = "Complete Level 9 (Magma Cavern Finale)",
            primaryColor = Color(0xFFA855F7),
            secondaryColor = Color(0xFF240046),
            accentColor = ElectricCyan,
            auraColor = RoyalViolet
        ),
        CharacterSkin(
            id = SkinId.GOLDEN_MONARCH,
            name = "Golden Monarch",
            subtitle = "Royal Crown Sovereign",
            unlockHint = "Complete Level 12 (Starlight Nebula Finale)",
            primaryColor = SunlitGold,
            secondaryColor = Color(0xFF543C00),
            accentColor = Color(0xFFFFF3B0),
            auraColor = SunlitGold
        ),
        CharacterSkin(
            id = SkinId.PRISMATIC_PHOENIX,
            name = "Prismatic Phoenix",
            subtitle = "Mythic Rainbow Crest",
            unlockHint = "Mastery Reward: Collect 24+ Stars",
            primaryColor = Color(0xFFFF59B4),
            secondaryColor = Color(0xFF2C0E4F),
            accentColor = Color(0xFF67E8F9),
            auraColor = Color(0xFFF472B6)
        )
    )

    fun byId(id: String): CharacterSkin =
        all.firstOrNull { it.id.name == id } ?: all.first()
}

data class LevelDefinition(
    val levelNumber: Int,
    val title: String,
    val themeId: LevelThemeId,
    val maxShots: Int,
    val colorCount: Int,
    val oneStarScore: Int,
    val twoStarScore: Int,
    val threeStarScore: Int,
    val rewardCoins: Int,
    val unlockSkinId: SkinId? = null,
    val unlockRareBooster: BoosterType? = null,
    val unlockRareBoosterCount: Int = 0,
    val hasStoneObstacles: Boolean = false,
    val hasIceObstacles: Boolean = false
)

object LevelCatalog {
    val levels: List<LevelDefinition> = listOf(
        LevelDefinition(
            levelNumber = 1,
            title = "Sunlit Shallows",
            themeId = LevelThemeId.CORAL_LAGOON,
            maxShots = 24,
            colorCount = 4,
            oneStarScore = 900,
            twoStarScore = 1700,
            threeStarScore = 2600,
            rewardCoins = 60,
            unlockRareBooster = BoosterType.BOMB_ORB,
            unlockRareBoosterCount = 1
        ),
        LevelDefinition(
            levelNumber = 2,
            title = "Pearl Reefs",
            themeId = LevelThemeId.CORAL_LAGOON,
            maxShots = 25,
            colorCount = 4,
            oneStarScore = 1100,
            twoStarScore = 2000,
            threeStarScore = 3000,
            rewardCoins = 75,
            unlockRareBooster = BoosterType.LIGHTNING_ORB,
            unlockRareBoosterCount = 1
        ),
        LevelDefinition(
            levelNumber = 3,
            title = "Tidal Crown",
            themeId = LevelThemeId.CORAL_LAGOON,
            maxShots = 26,
            colorCount = 5,
            oneStarScore = 1400,
            twoStarScore = 2400,
            threeStarScore = 3600,
            rewardCoins = 120,
            unlockSkinId = SkinId.FOREST_SPRITE,
            unlockRareBooster = BoosterType.SUPERNOVA_ORB,
            unlockRareBoosterCount = 1
        ),
        LevelDefinition(
            levelNumber = 4,
            title = "Mossy Archway",
            themeId = LevelThemeId.EMERALD_CANOPY,
            maxShots = 26,
            colorCount = 5,
            oneStarScore = 1500,
            twoStarScore = 2600,
            threeStarScore = 3800,
            rewardCoins = 90,
            hasStoneObstacles = true,
            unlockRareBooster = BoosterType.RAINBOW_ORB,
            unlockRareBoosterCount = 1
        ),
        LevelDefinition(
            levelNumber = 5,
            title = "Firefly Hollow",
            themeId = LevelThemeId.EMERALD_CANOPY,
            maxShots = 27,
            colorCount = 5,
            oneStarScore = 1700,
            twoStarScore = 2900,
            threeStarScore = 4200,
            rewardCoins = 100,
            hasStoneObstacles = true,
            unlockRareBooster = BoosterType.DOUBLE_SCORE,
            unlockRareBoosterCount = 1
        ),
        LevelDefinition(
            levelNumber = 6,
            title = "Ancient Elderwood",
            themeId = LevelThemeId.EMERALD_CANOPY,
            maxShots = 28,
            colorCount = 5,
            oneStarScore = 2000,
            twoStarScore = 3300,
            threeStarScore = 4700,
            rewardCoins = 150,
            hasStoneObstacles = true,
            unlockSkinId = SkinId.EMBER_DRAGON,
            unlockRareBooster = BoosterType.SUPERNOVA_ORB,
            unlockRareBoosterCount = 1
        ),
        LevelDefinition(
            levelNumber = 7,
            title = "Obsidian Gate",
            themeId = LevelThemeId.MAGMA_CAVERN,
            maxShots = 27,
            colorCount = 5,
            oneStarScore = 2100,
            twoStarScore = 3500,
            threeStarScore = 5000,
            rewardCoins = 115,
            hasIceObstacles = true,
            unlockRareBooster = BoosterType.BOMB_ORB,
            unlockRareBoosterCount = 2
        ),
        LevelDefinition(
            levelNumber = 8,
            title = "Cinder Spire",
            themeId = LevelThemeId.MAGMA_CAVERN,
            maxShots = 28,
            colorCount = 6,
            oneStarScore = 2300,
            twoStarScore = 3800,
            threeStarScore = 5400,
            rewardCoins = 130,
            hasStoneObstacles = true,
            hasIceObstacles = true,
            unlockRareBooster = BoosterType.LIGHTNING_ORB,
            unlockRareBoosterCount = 2
        ),
        LevelDefinition(
            levelNumber = 9,
            title = "Caldera Core",
            themeId = LevelThemeId.MAGMA_CAVERN,
            maxShots = 29,
            colorCount = 6,
            oneStarScore = 2600,
            twoStarScore = 4200,
            threeStarScore = 6000,
            rewardCoins = 180,
            hasStoneObstacles = true,
            hasIceObstacles = true,
            unlockSkinId = SkinId.NEBULA_VALKYRIE,
            unlockRareBooster = BoosterType.SUPERNOVA_ORB,
            unlockRareBoosterCount = 2
        ),
        LevelDefinition(
            levelNumber = 10,
            title = "Comet Drift",
            themeId = LevelThemeId.STARLIGHT_NEBULA,
            maxShots = 28,
            colorCount = 6,
            oneStarScore = 2800,
            twoStarScore = 4500,
            threeStarScore = 6400,
            rewardCoins = 150,
            hasIceObstacles = true,
            unlockRareBooster = BoosterType.EAGLE_EYE,
            unlockRareBoosterCount = 2
        ),
        LevelDefinition(
            levelNumber = 11,
            title = "Aurora Prism",
            themeId = LevelThemeId.STARLIGHT_NEBULA,
            maxShots = 29,
            colorCount = 6,
            oneStarScore = 3000,
            twoStarScore = 4900,
            threeStarScore = 7000,
            rewardCoins = 175,
            hasStoneObstacles = true,
            hasIceObstacles = true,
            unlockRareBooster = BoosterType.RAINBOW_ORB,
            unlockRareBoosterCount = 2
        ),
        LevelDefinition(
            levelNumber = 12,
            title = "Celestial Crown",
            themeId = LevelThemeId.STARLIGHT_NEBULA,
            maxShots = 30,
            colorCount = 6,
            oneStarScore = 3400,
            twoStarScore = 5500,
            threeStarScore = 7800,
            rewardCoins = 250,
            hasStoneObstacles = true,
            hasIceObstacles = true,
            unlockSkinId = SkinId.GOLDEN_MONARCH,
            unlockRareBooster = BoosterType.SUPERNOVA_ORB,
            unlockRareBoosterCount = 3
        )
    )

    fun get(levelNumber: Int): LevelDefinition =
        levels.firstOrNull { it.levelNumber == levelNumber } ?: levels.first()
}

data class StoreCoinPack(
    val id: String,
    val title: String,
    val coins: Int,
    val bonusLives: Int = 0,
    val bonusSupernovas: Int = 0,
    val priceLabel: String,
    val badge: String? = null
)

object StoreCatalog {
    val coinPacks: List<StoreCoinPack> = listOf(
        StoreCoinPack(
            id = "pouch_500",
            title = "Sparkling Coin Pouch",
            coins = 500,
            priceLabel = "$0.99"
        ),
        StoreCoinPack(
            id = "bundle_starter",
            title = "Hero Starter Chest",
            coins = 1000,
            bonusLives = 5,
            bonusSupernovas = 2,
            priceLabel = "$1.99",
            badge = "POPULAR"
        ),
        StoreCoinPack(
            id = "chest_1500",
            title = "Golden Treasure Chest",
            coins = 1800,
            priceLabel = "$2.99",
            badge = "+20% BONUS"
        ),
        StoreCoinPack(
            id = "vault_4000",
            title = "Royal Monarch Vault",
            coins = 4500,
            bonusLives = 5,
            bonusSupernovas = 3,
            priceLabel = "$6.99",
            badge = "BEST VALUE"
        )
    )
}
