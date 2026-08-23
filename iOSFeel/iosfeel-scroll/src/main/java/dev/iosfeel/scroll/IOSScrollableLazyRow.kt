package dev.iosfeel.scroll

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun IOSScrollableLazyRow(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    config: IOSScrollConfig = IOSScrollConfig(),
    flingObserver: IOSFlingObserver? = null,
    interactionState: IOSScrollInteractionState = rememberIOSScrollInteractionState(config),
    startFadeWidth: Dp = 0.dp,
    endFadeWidth: Dp = 0.dp,
    content: LazyListScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    interactionState.updateScope(coroutineScope)

    val flingBehavior = rememberIOSFlingBehavior(
        config = config,
        observer = flingObserver
    )

    val nestedConnection = remember(interactionState, coroutineScope) {
        IOSScrollNestedConnection(
            state = interactionState,
            coroutineScope = coroutineScope,
            orientation = IOSScrollOrientation.Horizontal
        )
    }

    // Safety guarantee 1: Watch when LazyListState finishes scrolling
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress }
            .collect { isScrolling ->
                if (!isScrolling && interactionState.overscroll != 0f && interactionState.phase != IOSScrollPhase.SpringingBack) {
                    interactionState.releaseOverscroll(0f, coroutineScope)
                }
            }
    }

    Box(
        modifier = modifier
            .iosFadingEdge(start = startFadeWidth, end = endFadeWidth)
            // Safety guarantee 2: Catch when all fingers are lifted off the screen
            .pointerInput(interactionState, coroutineScope) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val allPointersUp = event.changes.all { !it.pressed }
                        if (allPointersUp) {
                            if (interactionState.overscroll != 0f && interactionState.phase != IOSScrollPhase.SpringingBack) {
                                interactionState.releaseOverscroll(0f, coroutineScope)
                            }
                        }
                    }
                }
            }
    ) {
        LazyRow(
            modifier = Modifier
                .nestedScroll(nestedConnection)
                .graphicsLayer {
                    translationX = interactionState.overscroll
                },
            state = state,
            contentPadding = contentPadding,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            flingBehavior = flingBehavior,
            overscrollEffect = null,
            content = content
        )
    }
}
