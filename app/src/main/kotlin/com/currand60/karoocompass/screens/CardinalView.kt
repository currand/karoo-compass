package com.currand60.karoocompass.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import com.currand60.karoocompass.R
import androidx.glance.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentSize
import androidx.glance.layout.wrapContentWidth
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.hammerhead.karooext.models.ViewConfig
import kotlin.math.ceil
import kotlin.math.roundToInt

private fun mapDegreesToCardinal(degrees: Int): String {
    return when (degrees) {
        in 0..22 -> "N"
        in 23..67 -> "NE"
        in 68..112 -> "E"
        in 113..157 -> "SE"
        in 158..202 -> "S"
        in 203..247 -> "SW"
        in 248..292 -> "W"
        in 293..337 -> "NW"
        else -> "N"
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun CardinalView (
    context: Context,
    degrees: Double,
    config: ViewConfig,
){
    var topRowPadding = 0f
    var bottomTextPadding = 0f
    var finalTextSize: Float = config.textSize.toFloat()

    val viewHeightInDp: Float = ceil(config.viewSize.second / context.resources.displayMetrics.density)

    val textAlignment: TextAlign = when (config.alignment) {
        ViewConfig.Alignment.CENTER -> TextAlign.Center
        ViewConfig.Alignment.LEFT -> TextAlign.Start
        ViewConfig.Alignment.RIGHT -> TextAlign.End
    }
    val headerTextSize = TextUnit(17f, TextUnitType.Sp)

    val topRowHeight = 20f
    val bottomRowHeight: Float = viewHeightInDp - topRowHeight

    if (config.viewSize.first <= 238) {
        if (config.viewSize.second > 300) {
            bottomTextPadding += 11f
            finalTextSize -= 6f
        } else if (config.viewSize.second < 128) {
            //(238,126)
            topRowPadding += 4f
            bottomTextPadding += 6f
        } else {
            //(238,148)
            topRowPadding += 6f
            bottomTextPadding += 11f
            finalTextSize -= 6f
        }
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(start = 5.dp, end = 5.dp, top = topRowPadding.dp)
            .cornerRadius(8.dp)
            .background(Color.Transparent)
    ) {
        when (config.alignment) {
            ViewConfig.Alignment.CENTER ->
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(topRowHeight.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .wrapContentSize()
                            .padding(end = 2.dp, top = 4.dp),
                        provider = ImageProvider(
                            resId = R.drawable.compass,
                        ),
                        contentDescription =context.getString(R.string.cardinaldirection_title),
                        colorFilter = ColorFilter.tint(ColorProvider(Color(context.getColor(R.color.icon_green))))
                    )
                    Text(
                        modifier = GlanceModifier
                            .padding(end = 2.dp, top = 0.dp),
                        text = context.getString(R.string.cardinaldirection_title_short).uppercase(),
                        style = TextStyle(
                            color = ColorProvider(Color(context.getColor(R.color.text_color))),
                            fontSize = headerTextSize,
                            textAlign = textAlignment,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }

            ViewConfig.Alignment.LEFT ->
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(topRowHeight.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .padding(end = 2.dp, top = 0.dp),
                        text = context.getString(R.string.cardinaldirection_title_short).uppercase(),
                        style = TextStyle(
                            color = ColorProvider(Color(context.getColor(R.color.text_color))),
                            fontSize = headerTextSize,
                            textAlign = textAlignment,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                    Image(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .wrapContentSize()
                            .padding(end = 2.dp, top = 4.dp),
                        provider = ImageProvider(
                            resId = R.drawable.compass,
                        ),
                        contentDescription =context.getString(R.string.cardinaldirection_title),
                        colorFilter = ColorFilter.tint(ColorProvider(Color(context.getColor(R.color.icon_green))))
                    )
                }

            else ->
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(topRowHeight.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .wrapContentSize()
                            .padding(end = 2.dp, top = 4.dp),
                        provider = ImageProvider(
                            resId = R.drawable.compass,
                        ),
                        contentDescription =context.getString(R.string.cardinaldirection_title),
                        colorFilter = ColorFilter.tint(ColorProvider(Color(context.getColor(R.color.icon_green))))
                    )
                    Text(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .padding(end = 2.dp, top = 0.dp),
                        text = context.getString(R.string.cardinaldirection_title_short).uppercase(),
                        style = TextStyle(
                            color = ColorProvider(Color(context.getColor(R.color.text_color))),
                            fontSize = headerTextSize,
                            textAlign = textAlignment,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }
        }
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(bottomRowHeight.dp)
                .padding(start = 0.dp, end = 8.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = GlanceModifier
                    .padding(top = bottomTextPadding.dp)
                    .defaultWeight()
                    .fillMaxWidth(),
                text = mapDegreesToCardinal(degrees.toInt()),
                style = TextStyle(
                    color = ColorProvider(Color(context.getColor(R.color.text_color))),
                    fontSize = TextUnit(finalTextSize, TextUnitType.Sp),
                    textAlign = textAlignment,
                    fontFamily = FontFamily.Monospace,
                )
            )
        }
    }
}

@Suppress("unused")
@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = (238 / 1.9).toInt(), heightDp = (148 / 1.9).toInt())
@Composable
fun PreviewColorSpeedOverSpeedLevel5() {

    CardinalView(
        context = LocalContext.current,
        degrees = 125.0,
        config = ViewConfig(
            alignment = ViewConfig.Alignment.CENTER,
            textSize = 50,
            gridSize = Pair(30, 15),
            viewSize = Pair(238, 148),
            preview = true
        ),
    )
}