package com.kennycason.genetic.draw

/**
 * Created by kenny on 5/23/16.
 */

import com.kennycason.genetic.draw.gene.*
import com.kennycason.genetic.draw.gene.mutate.PixelIncrementalMutator
import com.kennycason.genetic.draw.gene.mutate.PolygonIncrementalMutator
import com.kennycason.genetic.draw.fitness.ImageDifference
import com.kennycason.genetic.draw.probability.StaticProbability
import com.sun.javafx.iio.ImageStorage
import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

fun main(args: Array<String>) {
    SingleParentPixelGeneticDraw().run()
}

class SingleParentPixelGeneticDraw {
    //val fileName = "kirby.jpg"
    val fileName = "grid.png"
    val target: BufferedImage = ImageIO.read(Thread.currentThread().contextClassLoader.getResource(fileName))
    val context = Context(
            width = target.width,
            height = target.height,
            geneCount = 1000,
            populationCount = -1,
            mutationProbability = StaticProbability(0.005f),
            pixelSize = 8)
    val mutator = PixelIncrementalMutator(context)
    val pixelGenetic = PixelGenetic(context)
    val genetic = Genetic(context)
    val fitnessFunction = ImageDifference()

    val canvas: BufferedImage = BufferedImage(context.width, context.height, BufferedImage.TYPE_INT_ARGB)
    val canvasGraphics = canvas.graphics
    val mostFitCanvas: BufferedImage = BufferedImage(context.width, context.height, BufferedImage.TYPE_INT_ARGB)
    val mostFitCanvasGraphics = mostFitCanvas.graphics

    fun run() {
        val frame = JFrame()
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        frame.setSize(context.width, context.height + 18)
        frame.setVisible(true)
        
        var mostFit = pixelGenetic.newIndividual()
        var mostFitScore = Double.MAX_VALUE

        val panel = object: JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                pixelGenetic.expressDna(mostFitCanvasGraphics, mostFit)
                g.drawImage(mostFitCanvas, 0, 0, context.width, context.height, this)
            }
        };
        frame.add(panel)
        panel.revalidate()

        println("sanity test (should = 0) " + fitnessFunction.compare(target, target))
        var i = 0
        do {
            val child = mutator.mutate(mostFit, context.mutationProbability.next())

            // evaluate fitness
            canvasGraphics.color = Color.BLACK
            canvasGraphics.clearRect(0, 0, context.width, context.height)
            pixelGenetic.expressDna(canvasGraphics, child)
            val fitness = fitnessFunction.compare(canvas, target)

            if (fitness <= mostFitScore) {
                println("$i, $fitness")
                mostFit = genetic.copy(child)
                mostFitScore = fitness
            }
            panel.repaint() // must redraw as that's what actually draws to the canvas
            i++
        } while (mostFitScore > 0)
        ImageIO.write(mostFitCanvas, "png", File("/tmp/evolved.png"))
    }

}