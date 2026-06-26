package live.hails.hailsdotgo.ui.iv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import live.hails.hailsdotgo.ui.theme.IVGold
import live.hails.hailsdotgo.ui.theme.IVGreen
import live.hails.hailsdotgo.ui.theme.IVTeal

@Composable
fun IVResultScreen(
    vm: IVResultViewModel = viewModel(),
    onDismiss: (() -> Unit)? = null,
) {
    val form   by vm.form.collectAsState()
    val result by vm.result.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text("IV Calculator", style = MaterialTheme.typography.headlineMedium)
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        }

        OutlinedTextField(
            value         = form.pokemonName,
            onValueChange = { vm.updateForm { copy(pokemonName = it) } },
            label         = { Text("Pokémon Name") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
        )

        val cpValue    = form.cp.toIntOrNull()
        val cpBlank    = form.cp.isBlank()
        val cpSuspect  = !cpBlank && cpValue != null && cpValue < 200
        val cpWarning  = Color(0xFFFFC107)  // amber

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value           = form.cp,
                onValueChange   = { vm.updateForm { copy(cp = it) } },
                label           = { Text("CP") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.weight(1f),
                colors          = if (cpSuspect) OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = cpWarning,
                    unfocusedBorderColor = cpWarning,
                    focusedLabelColor    = cpWarning,
                    unfocusedLabelColor  = cpWarning,
                ) else OutlinedTextFieldDefaults.colors(),
            )
            OutlinedTextField(
                value           = form.hp,
                onValueChange   = { vm.updateForm { copy(hp = it) } },
                label           = { Text("HP") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.weight(1f),
            )
        }
        when {
            cpBlank -> {
                val glowNote = if (form.isShadow || form.isPurified)
                    " Shadow / Purified glow can cover the digits." else ""
                Text(
                    "CP not detected. Scan again when clear.$glowNote",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            cpSuspect -> Text(
                "CP looks too low. Tap the field to correct it.",
                style = MaterialTheme.typography.bodySmall,
                color = cpWarning,
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value           = form.dustCost,
                onValueChange   = { vm.updateForm { copy(dustCost = it) } },
                label           = { Text("Dust") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.weight(1.4f),
            )
            OutlinedTextField(
                value           = form.trainerLevel,
                onValueChange   = { vm.updateForm { copy(trainerLevel = it) } },
                label           = { Text("Your Level") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.weight(1f),
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
                selected = form.isLucky,
                onClick  = { vm.updateForm { copy(isLucky = !isLucky, isShadow = false, isPurified = false) } },
                label    = { Text("★ Lucky") },
                modifier = Modifier.weight(1f),
            )
            FilterChip(
                selected = form.isShadow,
                onClick  = { vm.updateForm { copy(isShadow = !isShadow, isLucky = false, isPurified = false) } },
                label    = { Text("Shadow") },
                modifier = Modifier.weight(1f),
            )
            FilterChip(
                selected = form.isPurified,
                onClick  = { vm.updateForm { copy(isPurified = !isPurified, isLucky = false, isShadow = false) } },
                label    = { Text("Purified") },
                modifier = Modifier.weight(1f),
            )
        }

        Text("Best Stat", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("" to "Any", "atk" to "Atk", "def" to "Def", "sta" to "Sta").forEach { (v, label) ->
                FilterChip(
                    selected = form.topStat == v,
                    onClick  = { vm.updateForm { copy(topStat = v) } },
                    label    = { Text(label) },
                )
            }
        }

        Text("Appraisal", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
                selected = form.appraisalBars == null,
                onClick  = { vm.updateForm { copy(appraisalBars = null) } },
                label    = { Text("Skip") },
            )
            listOf(0, 1, 2, 3).forEach { n ->
                FilterChip(
                    selected = form.appraisalBars == n,
                    onClick  = { vm.updateForm { copy(appraisalBars = n) } },
                    label    = { Text("★$n") },
                )
            }
        }

        Button(
            onClick  = { vm.calculate() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Calculate IVs")
        }

        // ── Results ──────────────────────────────────────────────────────────
        when (val r = result) {
            is IVResultState.Loading ->
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))

            is IVResultState.Error ->
                Text(r.message, color = MaterialTheme.colorScheme.error)

            is IVResultState.Success -> {
                HorizontalDivider()
                val resp       = r.response
                val candidates = resp.candidates.orEmpty()

                if (candidates.isEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "No matches found",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            "Check the CP value and try adding appraisal bars to narrow the results.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    val ivPctMin = candidates.minOf { it.ivPct }
                    val ivPctMax = candidates.maxOf { it.ivPct }
                    val atkMin   = candidates.minOf { it.atkIv }
                    val atkMax   = candidates.maxOf { it.atkIv }
                    val defMin   = candidates.minOf { it.defIv }
                    val defMax   = candidates.maxOf { it.defIv }
                    val staMin   = candidates.minOf { it.staIv }
                    val staMax   = candidates.maxOf { it.staIv }
                    val levelMin = candidates.minOf { it.level }
                    val levelMax = candidates.maxOf { it.level }
                    val levelStr = if (levelMin == levelMax)
                        "L${"%.1f".format(levelMin)}"
                    else
                        "L${"%.1f".format(levelMin)}–${"%.1f".format(levelMax)}"

                    // ── Circular IV gauge ─────────────────────────────────
                    val gaugeColor = ivColor(ivPctMin)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier         = Modifier.size(148.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                progress    = { (ivPctMin / 100.0).toFloat() },
                                modifier    = Modifier.fillMaxSize(),
                                color       = gaugeColor,
                                trackColor  = gaugeColor.copy(alpha = 0.15f),
                                strokeWidth = 12.dp,
                                strokeCap   = StrokeCap.Round,
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val pctText = if (ivPctMin == ivPctMax)
                                    "${"%.1f".format(ivPctMin)}%"
                                else
                                    "${"%.0f".format(ivPctMin)}–${"%.0f".format(ivPctMax)}%"
                                Text(
                                    pctText,
                                    style      = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color      = gaugeColor,
                                )
                                Text(
                                    ivLabel(ivPctMin),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = gaugeColor,
                                )
                            }
                        }
                    }

                    // ── IV ranges per stat ────────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        IVStatColumn("Atk IV", atkMin, atkMax)
                        IVStatColumn("Def IV", defMin, defMax)
                        IVStatColumn("Sta IV", staMin, staMax)
                    }

                    // ── Summary line ──────────────────────────────────────
                    Text(
                        "${resp.pokemon.displayName} / CP ${form.cp} / HP ${form.hp} / $levelStr",
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    // ── Collapsible candidate list ─────────────────────────
                    if (candidates.size == 1) {
                        val c     = candidates.first()
                        val color = ivColor(c.ivPct)
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                "${c.atkIv}/${c.defIv}/${c.staIv}  L${"%.1f".format(c.level)}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                            ) {
                                Text("✓ Confirmed", color = IVGold,
                                    style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "${"%.1f".format(c.ivPct)}%",
                                    color      = color,
                                    fontWeight = FontWeight.Bold,
                                    style      = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    } else {
                        var expanded by remember { mutableStateOf(false) }
                        TextButton(
                            onClick  = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                if (expanded) "HIDE IV COMBINATIONS"
                                else "SHOW ALL ${candidates.size} IV COMBINATIONS",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                        if (expanded) {
                            candidates.forEach { c ->
                                val color = ivColor(c.ivPct)
                                Row(
                                    modifier              = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        "${c.atkIv}/${c.defIv}/${c.staIv}  L${"%.1f".format(c.level)}",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    Text(
                                        "${"%.1f".format(c.ivPct)}%",
                                        color      = color,
                                        fontWeight = FontWeight.Bold,
                                        style      = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun IVStatColumn(label: String, min: Int, max: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            if (min == max) "$min/15" else "$min–$max/15",
            style      = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun ivLabel(pct: Double): String = when {
    pct >= 100.0 -> "Perfect!"
    pct >= 93.3  -> "Amazing"
    pct >= 82.2  -> "Great IV!"
    pct >= 66.7  -> "Good"
    else         -> "OK"
}

private fun ivColor(pct: Double): Color = when {
    pct >= 100.0 -> IVGold
    pct >= 90.0  -> IVGreen
    pct >= 80.0  -> IVTeal
    else         -> Color.Unspecified
}
