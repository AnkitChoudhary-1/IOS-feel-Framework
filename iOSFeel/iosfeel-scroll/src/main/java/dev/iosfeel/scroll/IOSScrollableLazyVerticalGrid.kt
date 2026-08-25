package dev.iosfeel.scroll

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun IOSScrollableLazyVerticalGrid(
    columns: GridCells,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    config: IOSScrollConfig = IOSScrollConfig(),
    flingObserver: IOSFlingObserver? = null,
    interactionState: IOSScrollInteractionState = rememberIOSScrollInteractionState(config),
    topFadeHeight: Dp = 0.dp,
    bottomFadeHeight: Dp = 0.dp,
    content: LazyGridScope.() -> Unit
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
            orientation = IOSScrollOrientation.Vertical
        )
    }

    // Safety guarantee 1: Watch when LazyGridState finishes scrolling
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
            .iosFadingEdge(top = topFadeHeight, bottom = bottomFadeHeight)
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
        LazyVerticalGrid(
            columns = columns,
            modifier = Modifier
                .nestedScroll(nestedConnection)
                .graphicsLayer {
                    translationY = interactionState.overscroll
                },
            state = state,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
            flingBehavior = flingBehavior,
            overscrollEffect = null,
            content = content
        )
    }
}
