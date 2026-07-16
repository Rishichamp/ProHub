package com.prohub.assistant.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * The full ProHub neural P+H logomark, faithfully reproduced from the brand
 * design system's vector paths (1024x1024 source space). Intended for large
 * in-app placements (splash screen, onboarding hero, About section) — NOT the
 * launcher icon, which uses a simplified derivative for small-size legibility.
 */
@Composable
fun ProHubLogo(
    modifier: Modifier = Modifier,
    color: Color = ProHubColors.Indigo
) {
    Canvas(modifier = modifier) {
        val scale = size.minDimension / 1024f
        fun p(x: Float, y: Float) = Offset(x * scale, y * scale)

        val strokeWidth = 48f * scale
        val neuralStrokeWidth = 16f * scale
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val neuralStroke = Stroke(width = neuralStrokeWidth, cap = StrokeCap.Round)

        // Left & right verticals (P stem / H sides)
        drawLine(color, p(280f, 320f), p(280f, 700f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color, p(744f, 320f), p(744f, 700f), strokeWidth = strokeWidth, cap = StrokeCap.Round)

        // P bowl
        val pBowl = Path().apply {
            moveTo(p(280f, 320f).x, p(280f, 320f).y)
            quadraticBezierTo(p(400f, 280f).x, p(400f, 280f).y, p(480f, 320f).x, p(480f, 320f).y)
            quadraticBezierTo(p(560f, 360f).x, p(560f, 360f).y, p(480f, 420f).x, p(480f, 420f).y)
            quadraticBezierTo(p(380f, 460f).x, p(380f, 460f).y, p(280f, 420f).x, p(280f, 420f).y)
        }
        drawPath(pBowl, color, style = stroke)

        // H horizontal bar
        drawLine(color, p(280f, 500f), p(744f, 500f), strokeWidth = strokeWidth, cap = StrokeCap.Round)

        // H upper-right bowl
        val hBowl = Path().apply {
            moveTo(p(744f, 320f).x, p(744f, 320f).y)
            quadraticBezierTo(p(650f, 280f).x, p(650f, 280f).y, p(580f, 320f).x, p(580f, 320f).y)
            quadraticBezierTo(p(520f, 360f).x, p(520f, 360f).y, p(580f, 420f).x, p(580f, 420f).y)
            quadraticBezierTo(p(660f, 460f).x, p(660f, 460f).y, p(744f, 420f).x, p(744f, 420f).y)
        }
        drawPath(hBowl, color, style = stroke)

        // Bottom hexagonal connectors
        drawLine(color, p(280f, 700f), p(380f, 760f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color, p(744f, 700f), p(640f, 760f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color, p(380f, 760f), p(512f, 820f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color, p(640f, 760f), p(512f, 820f), strokeWidth = strokeWidth, cap = StrokeCap.Round)

        // Neural connections
        val nodes = listOf(
            420f to 380f, 604f to 380f, 680f to 480f,
            344f to 480f, 460f to 620f, 564f to 620f
        )
        val hub = 512f to 510f
        nodes.forEach { (nx, ny) ->
            drawLine(color, p(hub.first, hub.second), p(nx, ny), strokeWidth = neuralStrokeWidth, cap = StrokeCap.Round)
        }
        drawLine(color, p(420f, 380f), p(604f, 380f), strokeWidth = neuralStrokeWidth, cap = StrokeCap.Round)
        drawLine(color, p(460f, 620f), p(564f, 620f), strokeWidth = neuralStrokeWidth, cap = StrokeCap.Round)

        // Neural nodes
        drawCircle(color, radius = 28f * scale, center = p(512f, 510f))
        drawCircle(color, radius = 24f * scale, center = p(420f, 380f))
        drawCircle(color, radius = 24f * scale, center = p(604f, 380f))
        drawCircle(color, radius = 24f * scale, center = p(680f, 480f))
        drawCircle(color, radius = 24f * scale, center = p(344f, 480f))
        drawCircle(color, radius = 24f * scale, center = p(460f, 620f))
        drawCircle(color, radius = 24f * scale, center = p(564f, 620f))
    }
}