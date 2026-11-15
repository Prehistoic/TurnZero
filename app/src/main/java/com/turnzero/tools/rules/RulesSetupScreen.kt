package com.turnzero.tools.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.turnzero.core.ui.components.TZEmptyState
import com.turnzero.core.ui.components.parseBoldText

/**
 * Swipeable carousel of setup steps, one step per page, with page-indicator dots. [resetKey]
 * identifies the specificity selection: whenever it changes, the carousel jumps back to step one,
 * because the step list it was paging through no longer applies.
 */
@Composable
fun SetupCarousel(
    steps: List<RuleItem>,
    resetKey: Any,
    modifier: Modifier = Modifier,
) {
    if (steps.isEmpty()) {
        TZEmptyState(message = "No setup steps available", modifier = modifier)
        return
    }

    val pagerState = rememberPagerState(pageCount = { steps.size })
    // A specificity change rebuilds the step list, so start again from the first step.
    LaunchedEffect(resetKey) { pagerState.scrollToPage(0) }

    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 0,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("SetupCarouselPager"),
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${page + 1}.",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(24.dp))
                SetupStepContent(item = steps[page])
            }
        }
        StepDots(
            count = steps.size,
            current = pagerState.currentPage,
            modifier = Modifier.padding(vertical = 12.dp),
        )
    }
}

/**
 * One setup step, laid out for the carousel: the title is centred above the content with extra
 * room below it, and every paragraph gets its own breathing room instead of the compact list
 * spacing used elsewhere.
 */
@Composable
private fun SetupStepContent(item: RuleItem, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        item.title?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
        }
        if (item.text.isNotEmpty() || item.bullets.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item.text.forEach { paragraph ->
                    Text(
                        text = remember(paragraph) { parseBoldText(paragraph) },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                item.bullets.forEach { bullet ->
                    Text(
                        text = remember(bullet) { bulletText(bullet) },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
        item.image?.let { assetPath ->
            Spacer(Modifier.height(12.dp))
            RulesAssetImage(assetPath = assetPath)
        }
    }
}

@Composable
private fun StepDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val selected = index == current
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        }
                    ),
            )
        }
    }
}
