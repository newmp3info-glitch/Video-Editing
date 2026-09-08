package com.rakibul.videostudio

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.media3.common.C
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.ChannelMixingAudioProcessor
import androidx.media3.common.audio.ChannelMixingMatrix
import androidx.media3.common.audio.SpeedProvider
import androidx.media3.effect.Brightness
import androidx.media3.effect.CanvasOverlay
import androidx.media3.effect.Contrast
import androidx.media3.effect.Crop
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.Effects
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.media3.common.util.UnstableApi
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

@UnstableApi
class ExportEngine(private val context: Context) {
    fun export(clips: List<Clip>, audio: List<AudioTrack>, overlays: List<Sticker>, crop: String, brightness: Float, contrast: Float, quality: String, callback: (Boolean,String)->Unit) {
        if (clips.isEmpty()) { callback(false,"Import a video first."); return }
        val outDir=File(context.getExternalFilesDir(null),"exports").apply{mkdirs()}
        val stamp=SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(Date())
        val out=File(outDir,"Rakibul_$stamp.mp4")
        val edited=clips.map { clip ->
            val media=MediaItem.Builder().setUri(clip.uri).setClippingConfiguration(MediaItem.ClippingConfiguration.Builder().setStartPositionMs(clip.trimStartMs).setEndPositionMs(clip.trimEndMs).build()).build()
            val effects=mutableListOf<Effect>()
            val b=(brightness + if(clip.effect=="Warm") .08f else 0f).coerceIn(-1f,1f)
            val c=(contrast + if(clip.effect=="Vivid") .12f else 0f).coerceIn(.5f,2f)
            if(abs(b)>.01f) effects += Brightness(b)
            if(abs(c-1f)>.01f) effects += Contrast(c)
            effectFor(clip.effect,clip.effectIntensity)?.let{effects+=it}
            cropEffect(if(clip.crop!="9:16") clip.crop else crop)?.let{effects+=it}
            presentationEffect(quality,if(clip.crop!="9:16") clip.crop else crop).let{effects+=it}
            val builder=EditedMediaItem.Builder(media).setEffects(Effects(emptyList(),effects))
            if(clip.speed!=1f) builder.setSpeed(object:SpeedProvider{override fun getSpeed(presentationTimeUs:Long)=clip.speed;override fun getNextSpeedChangeTimeUs(timeUs:Long)=C.TIME_UNSET})
            builder.build()
        }
        val sequences=mutableListOf(EditedMediaItemSequence.withAudioAndVideoFrom(edited))
        audio.forEach { track ->
            val processors=mutableListOf<AudioProcessor>()
            if(track.volume!=1f){ val p=ChannelMixingAudioProcessor(); val v=track.volume.coerceIn(0f,2f); p.putChannelMixingMatrix(ChannelMixingMatrix(1,1,floatArrayOf(v))); p.putChannelMixingMatrix(ChannelMixingMatrix(2,2,floatArrayOf(v,0f,0f,v))); processors+=p }
            val a=EditedMediaItem.Builder(MediaItem.fromUri(track.uri)).setRemoveVideo(true).setEffects(Effects(processors,emptyList())).build()
            sequences += EditedMediaItemSequence.Builder(setOf(C.TRACK_TYPE_AUDIO)).addItem(a).setIsLooping(true).build()
        }
        val compositionBuilder=Composition.Builder(sequences)
        buildStickerEffects(overlays)?.let{compositionBuilder.setEffects(Effects(emptyList(),listOf(it)))}
        val transformer=Transformer.Builder(context).setVideoMimeType(MimeTypes.VIDEO_H264).setAudioMimeType(MimeTypes.AUDIO_AAC).addListener(object:Transformer.Listener{
            override fun onCompleted(composition:Composition,exportResult:ExportResult){callback(true,"Exported: ${out.absolutePath}")}
            override fun onError(composition:Composition,exportResult:ExportResult,exportException:ExportException){callback(false,"Export failed: ${exportException.message?:"unknown error"}")}
        }).build()
        runCatching{transformer.start(compositionBuilder.build(),out.absolutePath)}.onFailure{callback(false,"Export failed: ${it.message?:"unknown error"}")}
    }

    private fun effectFor(name:String,intensity:Float):Effect? = when(name){
        "Lightning" -> OverlayEffect(listOf(FlashOverlay(intensity)))
        "Flash Shake" -> OverlayEffect(listOf(FlashOverlay((intensity*.8f).coerceIn(0f,1f))))
        "Mono" -> androidx.media3.effect.HslAdjustment(0f,-1f,0f)
        else -> null
    }
    private fun cropEffect(crop:String):Crop?=when(crop){"16:9"->Crop(-1f,1f,-.7778f,.7778f);"9:16"->Crop(-.5625f,.5625f,-1f,1f);"1:1"->Crop(-1f,1f,-1f,1f);"4:5"->Crop(-.8f,.8f,-1f,1f);"4:3"->Crop(-1f,1f,-.75f,.75f);else->null}
    private fun presentationEffect(quality:String,crop:String):Presentation{val base=when(quality){"720p"->720;"1080p"->1080;else->2160};val(w,h)=when(crop){"9:16"->base to base*16/9;"1:1"->base to base;"4:5"->base to base*5/4;"4:3"->base to base*3/4;else->base*16/9 to base};return Presentation.createForWidthAndHeight(w.toInt(),h.toInt(),Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP)}
}

@UnstableApi
private class FlashOverlay(private val intensity:Float):CanvasOverlay(true){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=android.graphics.Color.WHITE}
    override fun onDraw(canvas:Canvas,presentationTimeUs:Long){
        val ms=presentationTimeUs/1000L
        if(ms>220L)return
        val a=((1f-ms/220f).coerceIn(0f,1f)*intensity*230f).toInt()
        paint.alpha=a
        canvas.drawRect(0f,0f,canvas.width.toFloat(),canvas.height.toFloat(),paint)
    }
}

@UnstableApi
private class AnimatedStickerOverlay(private val sticker:Sticker):CanvasOverlay(true){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{typeface=Typeface.DEFAULT_BOLD;textAlign=Paint.Align.CENTER}
    override fun onDraw(canvas:Canvas,presentationTimeUs:Long){val ms=presentationTimeUs/1000L;val local=ms-sticker.atMs;if(local<0||local>sticker.durationMs)return;val f=local.toFloat()/sticker.durationMs.coerceAtLeast(1L);val entrance=(f/.18f).coerceIn(0f,1f);val exit=if(f>.86f)((1f-f)/.14f).coerceIn(0f,1f)else 1f;paint.alpha=(255f*min(entrance,exit)).toInt();val animScale=if(sticker.animation=="Bounce")1f+.10f*kotlin.math.sin(f*18.0)else .72+.28*entrance;val h=canvas.height.toFloat();val size=h*.075f*sticker.scale*animScale;paint.textSize=size;val x=canvas.width*sticker.x;val y=h*sticker.y;canvas.save();canvas.rotate(if(sticker.animation=="Wiggle")kotlin.math.sin(f*18.0).toFloat()*5f else 0f,x,y);if(sticker.emoji.isNotBlank())canvas.drawText(sticker.emoji,x,y,paint);if(sticker.label.isNotBlank()){paint.textSize=h*.022f;paint.color=android.graphics.Color.WHITE;canvas.drawText(sticker.label,x,y+size*.55f,paint)};canvas.restore()}
}

@UnstableApi
private fun buildStickerEffects(stickers:List<Sticker>):Effect?=if(stickers.isEmpty())null else OverlayEffect(stickers.map{AnimatedStickerOverlay(it)})
