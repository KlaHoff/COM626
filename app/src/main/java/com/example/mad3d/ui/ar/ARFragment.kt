package com.example.mad3d.ui.ar

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mad3d.R
import freemap.openglwrapper.GLMatrix

class ARFragment : Fragment() {

    private var permissions = arrayOf(Manifest.permission.CAMERA)
    private var surfaceTexture: SurfaceTexture? = null
    private lateinit var openglview: OpenGLView
    private lateinit var orientationManager: OrientationManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ar, container, false)
        openglview = view.findViewById(R.id.opengl_view)
        orientationManager = OrientationManager(requireContext())
        openglview.onTextureAvailableCallback = {
            Log.d("MAD3D", "Starting camera")
            surfaceTexture = it
            if (!startCamera()) {
                requestPermissions(permissions, 0)
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        orientationManager.startListening()
    }

    override fun onPause() {
        super.onPause()
        orientationManager.stopListening()
    }

    private fun checkPermissions(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startCamera()
        } else {
            AlertDialog.Builder(requireContext()).setPositiveButton("OK", null)
                .setMessage("Will not work as camera permission not granted").show()
        }
    }

    private fun startCamera(): Boolean {
        Log.d("MAD3D", "startCamera()")
        if (checkPermissions()) {
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

    private fun updateOrientationMatrix() {
        val sensorMatrix = orientationManager.getRotationMatrix()
        val remappedSensorMatrix = GLMatrix(sensorMatrix)
        openglview.orientationMatrix = remappedSensorMatrix
    }
}
