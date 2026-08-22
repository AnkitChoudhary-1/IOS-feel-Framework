package dev.iosfeel.scroll

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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

@Composable
fun IOSScrollableLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    config: IOSScrollConfig = IOSScrollConfig(),
    flingObserver: IOSFlingObserver? = null,
    interactionState: IOSScrollInteractionState = rememberIOSScrollInteractionState(config),
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
            coroutineScope = coroutineScope
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
        LazyColumn(
            modifier = Modifier
                .nestedScroll(nestedConnection)
                .graphicsLayer {
                    translationY = interactionState.overscroll
                },
            state = state,
            flingBehavior = flingBehavior,
            overscrollEffect = null,
            content = content
        )
    }
}
