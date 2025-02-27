package org.thesis.project.Model

import java.awt.image.BufferedImage

class ImageHandler (
    axial: List<BufferedImage>,
    coronal: List<BufferedImage>,
    sagittal: List<BufferedImage>
){
    private val _axial: MutableList<BufferedImage> = axial.toMutableList()
    private val _sagittal: MutableList<BufferedImage> = sagittal.toMutableList()
    private val _coronal: MutableList<BufferedImage> = coronal.toMutableList()

    val axial: List<BufferedImage> = _axial
    val sagittal: List<BufferedImage> = _sagittal
    val coronal: List<BufferedImage> = _coronal

    private var _sliceNumber = 0
    var sliceNumber: Int
        get() = _sliceNumber
        set(value) {
            _sliceNumber = value
        }

    fun getSlice(viewType: ViewType): BufferedImage? {
        return when (viewType) {
            ViewType.AXIAL -> _axial.getOrNull(_sliceNumber)
            ViewType.CORONAL -> _coronal.getOrNull(_sliceNumber)
            ViewType.SAGITTAL -> _sagittal.getOrNull(_sliceNumber)
        }
    }

}

enum class ViewType { AXIAL, CORONAL, SAGITTAL }