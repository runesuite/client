package com.runesuite.client.game.live

import com.runesuite.client.base.Client.accessor
import com.runesuite.client.base.access.XGameShell
import com.runesuite.client.base.access.XGraphicsProvider
import com.runesuite.client.ext.java.swing.create2D
import hu.akarnokd.rxjava2.swing.SwingObservable
import io.reactivex.Observable
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.event.ComponentEvent
import java.awt.event.FocusEvent

interface Canvas {

    val shape: Rectangle

    object Live : Canvas {

        init {
            accessor.gameDrawingMode = 2
        }

        private val desktopHints = Toolkit.getDefaultToolkit()
                        .getDesktopProperty("awt.font.desktophints") as Map<*, *>?

        override val shape get() = Rectangle(accessor.canvas.size)

        val repaints: Observable<Graphics2D> = XGraphicsProvider.drawFull0.enter.map { me ->
            val gp = me.instance as XGraphicsProvider
            val g2d = gp.image.graphics as Graphics2D
            desktopHints?.let { g2d.addRenderingHints(it) }
            g2d
        }.publish().refCount().map { it.create2D() }

        /**
         * @see[java.awt.event.FocusListener]
         */
        val focusEvents: Observable<FocusEvent> = XGameShell.replaceCanvas.exit.map { accessor.canvas }.startWith(accessor.canvas)
                .flatMap { SwingObservable.focus(it) }

        /**
         * @see[java.awt.event.ComponentListener]
         */
        val componentEvents: Observable<ComponentEvent> = XGameShell.replaceCanvas.exit.map { accessor.canvas }.startWith(accessor.canvas)
                .flatMap { SwingObservable.component(it) }

        override fun toString(): String {
            return "Canvas.Live(shape=$shape)"
        }
    }

    companion object {
        val FIXED = Copy(Rectangle(0, 0, 765, 503))
    }

    fun copyOf(): Copy = Copy(shape)

    data class Copy(override val shape: Rectangle): Canvas
}