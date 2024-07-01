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

    // this variable will hold the texture where the camera feed will be shown
    private var surfaceTexture: SurfaceTexture? = null
    // this is our custom view that will show the 3d graphics
    private lateinit var openglview: OpenGLView
    private lateinit var orientationMessage: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ar, container, false)
        openglview = view.findViewById(R.id.opengl_view)
        orientationMessage = view.findViewById(R.id.orientation_message)

        // this sets a callback to start the camera when the texture is ready
        openglview.onTextureAvailableCallback = {
            Log.d("MAD3D", "Starting camera")
            surfaceTexture = it
            // check if we have camera permissions, if not, request them
            if (!startCamera()) {
                PermissionsUtils.requestPermissions(this, arrayOf(Manifest.permission.CAMERA))
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        // update the orientation message every time the fragment is resumed
        updateOrientationMessage()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // handle the result of the permissions request
        PermissionsUtils.onRequestPermissionsResult(grantResults) { granted ->
            if (granted) {
                // if permission is granted, start the camera
                startCamera()
            } else {
                // show an alert if the permission is not granted
                AlertDialog.Builder(requireContext()).setPositiveButton("OK", null)
                    .setMessage("Will not work as camera permission not granted").show()
            }
        }
    }

    private fun startCamera(): Boolean {
        Log.d("MAD3D", "startCamera()")
        // check if the app has camera permissions
        if (PermissionsUtils.checkPermissions(requireContext(), arrayOf(Manifest.permission.CAMERA))) {
            Log.d("MAD3D", "startCamera() ready to go")
            // get the camera provider
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                // set up the preview configuration
                val preview = Preview.Builder().build().also {
                    val provider: (SurfaceRequest) -> Unit = { request ->
                        val resolution = request.resolution
                        surfaceTexture?.apply {
                            setDefaultBufferSize(resolution.width, resolution.height)
                            val surface = Surface(this)
                            // provide the surface for the camera preview
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
                    // unbind all use cases before rebinding
                    cameraProvider.unbindAll()
                    // bind the camera to the lifecycle
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
        // check the device orientation and show/hide the message accordingly
        // it's part of the logic to only show the message in portrait mode
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientationMessage.visibility = View.VISIBLE
        } else {
            orientationMessage.visibility = View.GONE
        }
    }
}
