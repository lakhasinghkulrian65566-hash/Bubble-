package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameRepository
import com.example.data.PlayerSnapshot
import com.example.model.BoosterType
import com.example.model.StoreCatalog
import com.example.model.StoreCoinPack
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.CosmicMidnight
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.RubyHeart
import com.example.ui.theme.SunlitGold

@Composable
fun StoreScreen(
    snapshot: PlayerSnapshot,
    onBuyLifeRefill: (singleLife: Boolean) -> Unit,
    onBuyBooster: (BoosterType) -> Unit,
    onPurchaseCoinPack: (StoreCoinPack) -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingCoinPack by remember { mutableStateOf<StoreCoinPack?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicMidnight),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Treasure Store Banner
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, SunlitGold.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.height(160.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_store_banner_1794712843775),
                            contentDescription = stringResource(R.string.cd_store_banner),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xE60E0A24))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Treasure Emporium",
                                style = MaterialTheme.typography.headlineLarge,
                                color = SunlitGold
                            )
                            Text(
                                text = "Stock up on coins, refill hearts & buy temporary power-up boosters",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFF6F4FF)
                            )
                        }
                    }
                }
            }

            // Section 1: Life Refills
            item {
                SectionHeader(
                    title = "Heart & Life Refills",
                    subtitle = "Hearts regenerate every 15m, or refill instantly with coins"
                )
            }
            item {
                val lives = snapshot.profile.lives
                val maxLives = snapshot.profile.maxLives
                val isFull = lives >= maxLives
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LifeRefillCard(
                        title = "+1 Heart",
                        subtitle = "Quick Retry",
                        priceCoins = GameRepository.SINGLE_LIFE_COIN_PRICE,
                        enabled = !isFull && snapshot.profile.coins >= GameRepository.SINGLE_LIFE_COIN_PRICE,
                        isFull = isFull,
                        onClick = { onBuyLifeRefill(true) },
                        testTag = "store_buy_1_life_btn",
                        modifier = Modifier.weight(1f)
                    )
                    LifeRefillCard(
                        title = "Full 5/5 Refill",
                        subtitle = "Best Value",
                        priceCoins = GameRepository.FULL_LIVES_COIN_PRICE,
                        enabled = !isFull && snapshot.profile.coins >= GameRepository.FULL_LIVES_COIN_PRICE,
                        isFull = isFull,
                        onClick = { onBuyLifeRefill(false) },
                        testTag = "store_buy_full_lives_btn",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section 2: One-Time Boosters (Temporary Power-Ups)
            item {
                SectionHeader(
                    title = "One-Time Boosters",
                    subtitle = "Buying one-time items grants temporary power-ups for a level or shot"
                )
            }
            items(BoosterType.entries, key = { it.id }) { booster ->
                val owned = snapshot.boosters.firstOrNull { it.boosterId == booster.id }?.quantity ?: 0
                BoosterStoreCard(
                    booster = booster,
                    ownedQty = owned,
                    canAfford = snapshot.profile.coins >= booster.coinPrice,
                    onBuy = { onBuyBooster(booster) }
                )
            }

            // Section 3: In-App Coin Packs & Bundles
            item {
                SectionHeader(
                    title = "Coin Packs & Bundles",
                    subtitle = "In-app store packages (simulated demo checkout)"
                )
            }
            items(StoreCatalog.coinPacks, key = { it.id }) { pack ->
                CoinPackCard(
                    pack = pack,
                    onSelect = { pendingCoinPack = pack }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // Simulated In-App Purchase Confirmation Dialog
    val packToBuy = pendingCoinPack
    if (packToBuy != null) {
        AlertDialog(
            onDismissRequest = { pendingCoinPack = null },
            containerColor = CosmicIndigo,
            title = {
                Text(packToBuy.title, style = MaterialTheme.typography.headlineMedium, color = SunlitGold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Includes +${packToBuy.coins} Coins" +
                            (if (packToBuy.bonusLives > 0) ", Full Hearts Refill" else "") +
                            (if (packToBuy.bonusSupernovas > 0) ", and +${packToBuy.bonusSupernovas} Rare Supernova Boosters!" else "."),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CosmicSurface
                    ) {
                        Text(
                            text = "Demo In-App Purchase (${packToBuy.priceLabel}): No real payment is charged in this preview.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ElectricCyan,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPurchaseCoinPack(packToBuy)
                        pendingCoinPack = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldMint,
                        contentColor = CosmicMidnight
                    ),
                    modifier = Modifier.testTag("confirm_coin_pack_btn")
                ) {
                    Text("Confirm (${packToBuy.priceLabel})")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingCoinPack = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LifeRefillCard(
    title: String,
    subtitle: String,
    priceCoins: Int,
    enabled: Boolean,
    isFull: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CosmicIndigo,
        border = BorderStroke(1.2.dp, RubyHeart.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = RubyHeart,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RubyHeart,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag)
            ) {
                if (isFull) {
                    Text("Full", style = MaterialTheme.typography.labelLarge)
                } else {
                    Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = SunlitGold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("$priceCoins", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun BoosterStoreCard(
    booster: BoosterType,
    ownedQty: Int,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    val icon = when (booster) {
        BoosterType.EAGLE_EYE -> Icons.Filled.Visibility
        BoosterType.DOUBLE_SCORE -> Icons.Filled.Star
        BoosterType.EXTRA_SHOTS -> Icons.Filled.Add
        BoosterType.BOMB_ORB -> Icons.Filled.Whatshot
        BoosterType.LIGHTNING_ORB -> Icons.Filled.Bolt
        BoosterType.RAINBOW_ORB -> Icons.Filled.AutoAwesome
        BoosterType.SUPERNOVA_ORB -> Icons.Filled.AutoAwesome
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CosmicIndigo,
        border = BorderStroke(1.2.dp, booster.accentColor.copy(alpha = 0.55f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(booster.accentColor.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = booster.accentColor, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = booster.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = booster.accentColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = booster.durationLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = booster.accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = booster.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Owned: $ownedQty",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldMint
                )
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = onBuy,
                enabled = canAfford,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SunlitGold,
                    contentColor = CosmicMidnight
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier.testTag("store_buy_booster_${booster.id}")
            ) {
                Icon(Icons.Filled.MonetizationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("${booster.coinPrice}", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun CoinPackCard(
    pack: StoreCoinPack,
    onSelect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CosmicSurfaceElevated,
        border = BorderStroke(1.5.dp, if (pack.badge != null) SunlitGold else RoyalViolet.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(SunlitGold.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = SunlitGold, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(pack.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    pack.badge?.let { badge ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SunlitGold
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CosmicMidnight,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(3.dp))
                val extras = buildList {
                    add("+${pack.coins} Coins")
                    if (pack.bonusLives > 0) add("Full Hearts")
                    if (pack.bonusSupernovas > 0) add("+${pack.bonusSupernovas} Supernovas")
                }.joinToString(" • ")
                Text(
                    text = extras,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElectricCyan
                )
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = onSelect,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldMint,
                    contentColor = CosmicMidnight
                ),
                modifier = Modifier.testTag("store_buy_pack_${pack.id}")
            ) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(pack.priceLabel, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}
