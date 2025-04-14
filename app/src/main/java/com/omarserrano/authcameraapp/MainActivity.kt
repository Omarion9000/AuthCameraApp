package com.omarserrano.authcameraapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.*
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var photoImageView: ImageView
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var imageCapture: ImageCapture
    private val CAMERA_PERMISSION_CODE = 1001
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        previewView = findViewById(R.id.previewView)
        photoImageView = findViewById(R.id.photoImageView)

        findViewById<Button>(R.id.captureButton).setOnClickListener { capturePhoto() }
        findViewById<Button>(R.id.retrieveButton).setOnClickListener { retrievePhoto() }
        findViewById<Button>(R.id.googleSignInButton).setOnClickListener { signInWithGoogle() }
        findViewById<Button>(R.id.facebookSignUpBtn).setOnClickListener { signInWithFacebook() }
        findViewById<Button>(R.id.emailLoginButton).setOnClickListener {
            startActivity(Intent(this, EmailLoginActivity::class.java))
        }

        setupGoogleSignIn()
        setupFacebookSignIn()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Toast.makeText(this, "Camera start error", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto() {
        val photoFile = File(getExternalFilesDir(null), "photo.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(this@MainActivity, "Photo captured", Toast.LENGTH_SHORT).show()
                    uploadToFirebase(photoFile)
                    Glide.with(this@MainActivity).load(photoFile).into(photoImageView)
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("CAPTURE", "Capture failed: ${exc.message}", exc)
                    Toast.makeText(this@MainActivity, "Error capturing photo", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun uploadToFirebase(file: File) {
        val uri = Uri.fromFile(file)
        val storageRef = FirebaseStorage.getInstance().reference.child("images/${file.name}")
        storageRef.putFile(uri)
            .addOnSuccessListener {
                Toast.makeText(this, "Photo uploaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun retrievePhoto() {
        val storageRef = FirebaseStorage.getInstance().reference.child("images/photo.jpg")
        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                Glide.with(this).load(uri).into(photoImageView)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to retrieve photo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupFacebookSignIn() {
        callbackManager = CallbackManager.Factory.create()
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener {
                        Toast.makeText(this@MainActivity, if (it.isSuccessful) "Facebook login successful" else "Facebook login failed", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancel() {
                Toast.makeText(this@MainActivity, "Facebook login cancelled", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(this@MainActivity, "Facebook error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.result
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener(this) {
                        Toast.makeText(this, if (it.isSuccessful) "Google Sign-In successful" else "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Sign-In failed", e)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}