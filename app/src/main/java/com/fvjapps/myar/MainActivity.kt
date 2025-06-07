package com.fvjapps.myar

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position

import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    lateinit var sceneView: ARSceneView
    lateinit var loadingView: View
    lateinit var instructionText: TextView

    lateinit var controlsContainer: LinearLayout
    lateinit var scaleSlider: SeekBar
    lateinit var xPositionSlider: SeekBar
    lateinit var yPositionSlider: SeekBar
    lateinit var zPositionSlider: SeekBar
    lateinit var xRotationSlider: SeekBar
    lateinit var yRotationSlider: SeekBar
    lateinit var zRotationSlider: SeekBar
    var modelNode: ModelNode? = null

    private var selectedModel: Model =
        Model("models/computer_processor_chip.glb", "Computer Processor Chip", 0.3f)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFullScreen(
            findViewById(R.id.main),
            fullScreen = true,
            hideSystemBars = false,
            fitsSystemWindows = false
        )

//        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar)?.apply {
//            doOnApplyWindowInsets { systemBarsInsets ->
//                (layoutParams as ViewGroup.MarginLayoutParams).topMargin = systemBarsInsets.getInsets(
//                    WindowInsetsCompat.Type.systemBars()).top
//            }
//            title = ""
//        })
        supportActionBar.apply {
            title = "AR Core Demo"
        }

        instructionText = findViewById(R.id.instructionText)
        loadingView = findViewById(R.id.loadingView)
        sceneView = findViewById<ARSceneView>(R.id.sceneView).apply {
            lifecycle = this@MainActivity.lifecycle
            planeRenderer.isEnabled = true
            configureSession { session, config ->
                config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
                config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
            }
//            // automatic anchor placement
//
//            onSessionUpdated = { _, frame ->
//                if (anchorNode == null) {
//                    frame.getUpdatedPlanes()
//                        .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
//                        ?.let { plane ->
//                            addAnchorNode(plane.createAnchor(plane.centerPose))
//                        }
//                }
//            }

            onTrackingFailureChanged = { reason ->
                this@MainActivity.trackingFailureReason = reason
            }
        }

        sceneView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && anchorNode == null) {
                handleTap(event.x, event.y)
            }
            true
        }

        controlsContainer = findViewById(R.id.controls_menu)
        scaleSlider = findViewById(R.id.scaleSlider)
        xPositionSlider = findViewById(R.id.xPositionSlider)
        yPositionSlider = findViewById(R.id.yPositionSlider)
        zPositionSlider = findViewById(R.id.zPositionSlider)
        xRotationSlider = findViewById(R.id.xRotationSlider)
        yRotationSlider = findViewById(R.id.yRotationSlider)
        zRotationSlider = findViewById(R.id.zRotationSlider)

        scaleSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val scale = progress / 100f
                modelNode?.scale = Position(scale, scale, scale)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        xPositionSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val xPos = (progress - 100) / 50f // Range from -2 to +2
                modelNode?.position = modelNode?.position?.copy(x = xPos) ?: Position(xPos, 0f, 0f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        yPositionSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val yPos = (progress - 100) / 50f
                modelNode?.position = modelNode?.position?.copy(y = yPos) ?: Position(0f, yPos, 0f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        zPositionSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val zPos = (progress - 100) / 50f
                modelNode?.position = modelNode?.position?.copy(z = zPos) ?: Position(0f, 0f, zPos)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        xRotationSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                modelNode?.rotation = modelNode?.rotation?.copy(x = progress.toFloat()) ?: Position(progress.toFloat(), 0f, 0f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        yRotationSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                modelNode?.rotation = modelNode?.rotation?.copy(y = progress.toFloat()) ?: Position(0f, progress.toFloat(), 0f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        zRotationSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                modelNode?.rotation = modelNode?.rotation?.copy(z = progress.toFloat()) ?: Position(0f, 0f, progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.load_model -> {
                loadModel()
                return true
            }

            R.id.reset_model -> {
                modelNode?.let {
                    it.scale = Position(selectedModel.scale, selectedModel.scale, selectedModel.scale)
                    it.position = Position(0f, 0f, 0f)
                    it.rotation = Position(0f, 0f, 0f)
                    scaleSlider.progress = 100
                    xPositionSlider.progress = 100
                    yPositionSlider.progress = 100
                    zPositionSlider.progress = 100
                    xRotationSlider.progress = 0
                    yRotationSlider.progress = 0
                    zRotationSlider.progress = 0
                    true
                } ?: run {
                    Toast.makeText(this, "Place a model first.", Toast.LENGTH_SHORT).show()
                    false
                }
            }

            R.id.toggle_controls -> {
                if (anchorNode == null) {
                    Toast.makeText(this, "Place a model first", Toast.LENGTH_SHORT).show()
                } else {
                    controlsContainer.visibility =
                        if (controlsContainer.visibility == View.VISIBLE) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }
                }
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun loadModel() {
        val models: List<Model> = listOf(
            Model("models/computer_processor_chip.glb", "Computer Processor Chip", 0.3f),
            Model("models/t-rex.glb", "T-Rex", 6.0f),
            Model("models/ryo.glb", "Ryo", 4.0f)
        )

        var selectedIndex = models.indexOfFirst { it.modelPath == selectedModel.modelPath }
        if (selectedIndex == -1) selectedIndex = 0

        AlertDialog.Builder(this)
            .setTitle("Select a 3D Model")
            .setSingleChoiceItems(
                models.map { it.modelName }.toTypedArray(),
                selectedIndex
            ) { _, which ->
                selectedModel = models[which]
            }
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun handleTap(x: Float, y: Float) {
        val frame = sceneView.frame ?: return

        // Check if tracking is stable
        if (frame.camera.trackingState != TrackingState.TRACKING) {
            instructionText.text = "Move your phone slowly to initialize AR tracking"
            return
        }

        // Try to hit a horizontal plane
        val hitResult = frame.hitTest(x, y)
            .firstOrNull { hit ->
                val trackable = hit.trackable
                trackable is Plane &&
                        trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                        trackable.isPoseInPolygon(hit.hitPose)
            }

        if (hitResult != null) {
            val anchor = hitResult.createAnchor()
            addAnchorNode(anchor)
            instructionText.text = "" // Clear instructions after successful placement
        } else {
            instructionText.text = "Aim at a flat horizontal surface (e.g., table or floor)"
        }
    }

    fun addAnchorNode(anchor: Anchor) {
        sceneView.addChildNode(
            AnchorNode(sceneView.engine, anchor)
                .apply {
                    isEditable = true
                    lifecycleScope.launch {
                        isLoading = true
                        buildModelNode(selectedModel.modelPath, selectedModel.scale)?.let {
                            modelNode = it
                            addChildNode(it)
                            controlsContainer.visibility = View.VISIBLE
                            scaleSlider.progress = 100
                            xPositionSlider.progress = 100
                            yPositionSlider.progress = 100
                            zPositionSlider.progress = 100
                            xRotationSlider.progress = 0
                            yRotationSlider.progress = 0
                            zRotationSlider.progress = 0
                        }
                        isLoading = false
                    }
                    anchorNode = this
                }
        )
    }

    suspend fun buildModelNode(modelPath: String, scale: Float): ModelNode? {
        sceneView.modelLoader.loadModelInstance(modelPath)
            ?.let { modelInstance ->
                return ModelNode(
                    modelInstance = modelInstance,
                    scaleToUnits = scale,
                    centerOrigin = Position(y = -0.5f),
                ).apply {
                    isEditable = true
                    rotation = Position(0f, 0f, 0f)
                }
            }
        return null
    }


    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

    var anchorNode: AnchorNode? = null
        set(value) {
            if (field != value) {
                field = value
                if (value == null) {
                    controlsContainer.visibility = View.GONE
                    modelNode = null
                }
                updateInstructions()
            }
        }

    var trackingFailureReason: TrackingFailureReason? = null
        set(value) {
            if (field != value) {
                field = value
                updateInstructions()
            }
        }

    fun updateInstructions() {
        instructionText.text = trackingFailureReason?.let {
            it.getDescription(this)
        } ?: if (anchorNode == null) {
            getString(R.string.point_your_phone_down)
        } else {
            null
        }
    }

    fun AppCompatActivity.setFullScreen(
        rootView: View,
        fullScreen: Boolean,
        hideSystemBars: Boolean,
        fitsSystemWindows: Boolean
    ) {
        if (fullScreen) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

            if (hideSystemBars) {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
        }
        rootView.fitsSystemWindows = fitsSystemWindows
    }

    fun View.doOnApplyWindowInsets(
        action: (insets: WindowInsetsCompat) -> Unit
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            action(insets)
            insets
        }
        requestApplyInsets()
    }
}