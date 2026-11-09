package app.yongin.xr_circuit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Keep the main screen mounted so its initial loading proceeds behind the splash. */
@Composable
internal fun SplashHost(
    onSplashVisibilityChanged: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    var showingSplash by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(showingSplash) { onSplashVisibilityChanged(showingSplash) }
    LaunchedEffect(Unit) {
        if (showingSplash) {
            delay(700)
            showingSplash = false
        }
    }
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().then(if (showingSplash) Modifier.clearAndSetSemantics { } else Modifier)) {
            content()
        }
        if (showingSplash) {
            Box(
                modifier = Modifier.fillMaxSize().zIndex(1f).pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) awaitPointerEvent().changes.forEach { it.consume() }
                    }
                },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.splash_background),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize(),
                )
                Image(
                    painter = painterResource(R.drawable.splash_brand_mark),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(240.dp),
                )
            }
        }
    }
}
