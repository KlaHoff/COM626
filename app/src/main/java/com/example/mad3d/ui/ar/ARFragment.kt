package com.example.mad3d.ui.ar

import android.Manifest
import android.app.AlertDialog
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mad3d.R
import com.example.mad3d.utils.PermissionsUtils

class ARFragment : Fragment() {

    private var surfaceTexture: SurfaceTexture? = null
    private lateinit var openglview: OpenGLView
    private lateinit var orientationManager: OrientationManager
    private lateinit var orientationMessage: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ar, container, false)
        openglview = view.findViewById(R.id.opengl_view)
        orientationMessage = view.findViewById(R.id.orientation_message)
        orientationManager = OrientationManager(requireContext())
        openglview.onTextureAvailableCallback = {
            Log.d("MAD3D", "Starting camera")
            surfaceTexture = it
            if (!startCamera()) {
                PermissionsUtils.requestPermissions(this, arrayOf(Manifest.permission.CAMERA))
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        orientationManager.startListening()
        updateOrientationMessage()
    }

    override fun onPause() {
        super.onPause()
        orientationManager.stopListening()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtils.onRequestPermissionsResult(grantResults) { granted ->
            if (granted) {
                startCamera()
            } else {
                AlertDialog.Builder(requireContext()).setPositiveButton("OK", null)
                    .setMessage("Will not work as camera permission not granted").show()
            }
        }
    }

    private fun startCamera(): Boolean {
        Log.d("MAD3D", "startCamera()")
        if (PermissionsUtils.checkPermissions(requireContext(), arrayOf(Manifest.permission.CAMERA))) {
            Log.d("MAD3D", "startCamera() ready to go")
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    val provider: (SurfaceRequest) -> Unit = { request ->
                        val resolution = request.resolution
                        surfaceTexture?.apply {
                            setDefaultBufferSize(resolution.width, resolution.height)
                            val surface = Surface(this)
                            request.provideSurface(
                                surface,
                                ContextCompat.getMainExecutor(requireContext())
                            ) {
                                surface.release()
                            }
                        }
                    }
                    it.setSurfaceProvider(provider)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview)
                } catch (e: Exception) {
                    Log.e("MAD3D", e.stackTraceToString())
                }
            }, ContextCompat.getMainExecutor(requireContext()))
            return true
        } else {
            return false
        }
    }

    fun updateOrientationMessage() {
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientationMessage.visibility = View.VISIBLE
        } else {
            orientationMessage.visibility = View.GONE
        }
    }
}
